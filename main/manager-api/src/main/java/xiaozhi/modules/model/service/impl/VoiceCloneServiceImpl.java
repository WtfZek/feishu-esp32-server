package xiaozhi.modules.model.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.utils.ConvertUtils;
import xiaozhi.common.utils.OssUtils;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.model.client.VoiceCloneClient;
import xiaozhi.modules.model.dto.VoiceCloneDTO;
import xiaozhi.modules.model.dto.VoiceCloneRequestDTO;
import xiaozhi.modules.model.dto.VoiceCloneResponseDTO;
import xiaozhi.modules.model.service.VoiceCloneService;
import xiaozhi.modules.sys.service.SysUserService;
import xiaozhi.modules.timbre.entity.TimbreEntity;
import xiaozhi.modules.timbre.service.TimbreService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * 语音克隆服务实现
 *
 * @author zjy
 * @since 2025-3-21
 */
@Slf4j
@Service
public class VoiceCloneServiceImpl implements VoiceCloneService {

    @Autowired
    private VoiceCloneClient voiceCloneClient;
    
    @Autowired
    private OssUtils ossUtils;
    
    @Autowired
    private TimbreService timbreService;
    
    @Autowired
    private SysUserService userService;
    
    @Value("${aliyun.oss.voice-clone-dir:voice-clone}")
    private String voiceCloneDir;
    
    @Value("${aliyun.oss.voice-demo-dir:voice-demo}")
    private String voiceDemoDir;

    /**
     * 支持的音频文件类型
     */
    private static final String[] SUPPORTED_AUDIO_TYPES = {"wav", "mp3", "m4a"};

    @Override
    public Result<?> cloneVoice(VoiceCloneDTO dto) {
        // 1. 校验音频文件
        MultipartFile voiceFile = dto.getVoiceFile();
        validateAudioFile(voiceFile);

        try {
            // 2. 上传至OSS，获取URL
            log.info("开始上传音频文件到OSS...");
            String audioUrl = ossUtils.upload(voiceFile, voiceCloneDir);
            log.info("音频文件上传成功，URL: {}", audioUrl);

            // 3. 创建请求对象
            VoiceCloneRequestDTO request = new VoiceCloneRequestDTO();
            request.setUrl(audioUrl);

            // 4. 调用语音克隆服务
            log.info("调用语音克隆服务，请求参数: {}", request);
            VoiceCloneResponseDTO response = voiceCloneClient.cloneVoice(request);
            
            // 5. 处理返回结果
            if (response != null && response.getCode() == 200) {
                try {
                    String voiceId = response.getTts_voice();
                    
                    // 更新DTO中的字段
                    if (StringUtils.isNotBlank(voiceId)) {
                        dto.setTtsVoice(voiceId);
                    }
                    
                    // 直接使用上传的音频URL作为示例音频URL
                    dto.setVoiceDemo(audioUrl);
                    log.info("使用上传的音频URL作为示例音频，URL: {}", audioUrl);
                    
                    // 创建TimbreEntity并保存
                    saveTimbreEntity(dto);
                    
                    log.info("语音克隆成功，voiceId: {}", voiceId);
                    return new Result<>().ok(voiceId);
                } catch (Exception e) {
                    log.error("处理语音克隆结果异常", e);
                    throw new RenException("处理语音克隆结果失败");
                }
            }
            
            // 处理失败情况
            String errorMsg = response != null ? response.getMsg() : "语音克隆失败";
            log.error("语音克隆失败: {}", errorMsg);
            return new Result<>().error(errorMsg);
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            log.error("语音克隆过程发生异常", e);
            return new Result<>().error("语音克隆失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<?> processCloneFromFile(String name, String languages, String remark, File audioFile, Long userId) {
        log.info("开始处理支付成功后的语音克隆任务，音色名称: {}, 语言: {}, 用户ID: {}", name, languages, userId);
        
        try {
            // 校验参数
            if (StringUtils.isBlank(name)) {
                return new Result<>().error("音色名称不能为空");
            }
            
            if (StringUtils.isBlank(languages)) {
                return new Result<>().error("语言不能为空");
            }
            
            if (audioFile == null || !audioFile.exists()) {
                return new Result<>().error("音频文件不存在或已损坏");
            }
            
            // 校验文件大小（最大5MB）
            long maxSize = 5 * 1024 * 1024;
            if (audioFile.length() > maxSize) {
                return new Result<>().error("音频文件大小不能超过5MB");
            }
            
            // 校验文件类型
            try (FileInputStream fis = new FileInputStream(audioFile)) {
                String fileType = FileTypeUtil.getType(fis);
                boolean isValidType = false;
                
                for (String supportedType : SUPPORTED_AUDIO_TYPES) {
                    if (supportedType.equalsIgnoreCase(fileType)) {
                        isValidType = true;
                        break;
                    }
                }
                
                if (!isValidType) {
                    return new Result<>().error("不支持的音频格式，请上传wav、mp3或m4a格式的音频文件");
                }
            } catch (IOException e) {
                log.error("读取文件类型异常", e);
                return new Result<>().error("无法识别文件类型");
            }
            
            // 上传文件到OSS
            log.info("上传音频文件至OSS...");
            String audioUrl = ossUtils.uploadFile(audioFile, voiceCloneDir);
            log.info("音频文件上传成功，URL: {}", audioUrl);
            
            // 构建请求对象
            VoiceCloneRequestDTO request = new VoiceCloneRequestDTO();
            request.setUrl(audioUrl);
            
            // 调用语音克隆服务
            log.info("调用语音克隆服务，请求参数: {}", request);
            VoiceCloneResponseDTO response = voiceCloneClient.cloneVoice(request);
            
            // 处理返回结果
            if (response != null && response.getCode() == 200) {
                String voiceId = response.getTts_voice();
                
                // 创建TimbreEntity并保存
                TimbreEntity entity = new TimbreEntity();
                
                // 设置ID为TTS_FS_UUID简短版本，避免超出数据库字段长度
                String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                entity.setId("TTS_FS_" + uuid);
                
                // 设置基本属性
                entity.setName(name);
                entity.setLanguages(languages);
                entity.setRemark(remark);
                entity.setTtsVoice(voiceId);
                entity.setVoiceDemo(audioUrl);
                entity.setCreator(userId);
                
                // 设置固定属性
                entity.setTtsModelId("TTS_FeiShuTTS");
                entity.setSort(0);
                entity.setCreateDate(new Date());
                entity.setUpdateDate(new Date());
                
                // 保存到数据库
                log.info("保存音色实体到数据库: {}", entity);
                timbreService.insert(entity);
                log.info("音色实体保存成功，ID: {}", entity.getId());
                
                // 删除临时文件
                boolean deleted = FileUtil.del(audioFile);
                if (!deleted) {
                    log.warn("删除临时文件失败: {}", audioFile.getAbsolutePath());
                }
                
                log.info("语音克隆成功，voiceId: {}", voiceId);
                return new Result<>().ok(voiceId);
            }
            
            // 处理失败情况
            String errorMsg = response != null ? response.getMsg() : "语音克隆失败";
            log.error("语音克隆失败: {}", errorMsg);
            return new Result<>().error(errorMsg);
        } catch (Exception e) {
            log.error("处理支付后语音克隆任务异常", e);
            return new Result<>().error("语音克隆处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 将VoiceCloneDTO转换为TimbreEntity并保存
     * 
     * @param dto 语音克隆DTO
     */
    private void saveTimbreEntity(VoiceCloneDTO dto) {
        if (dto == null) {
            return;
        }
        
        try {
            // 创建TimbreEntity对象
            TimbreEntity entity = new TimbreEntity();
            
            // 设置ID为TTS_FS_UUID简短版本，避免超出数据库字段长度
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            entity.setId("TTS_FS_" + uuid);
            
            // 设置基本属性
            entity.setName(dto.getName());
            entity.setLanguages(dto.getLanguages());
            entity.setRemark(dto.getRemark());
            entity.setTtsVoice(dto.getTtsVoice());
            entity.setVoiceDemo(dto.getVoiceDemo());
            
            // 设置固定属性
            entity.setTtsModelId("TTS_FeiShuTTS");
            entity.setSort(0);
            
            // 保存到数据库
            log.info("保存音色实体到数据库: {}", entity);
            timbreService.insert(entity);
            log.info("音色实体保存成功，ID: {}", entity.getId());
        } catch (Exception e) {
            log.error("保存音色实体失败", e);
            throw new RenException("保存音色实体失败: " + e.getMessage());
        }
    }
    
    /**
     * 校验音频文件
     *
     * @param file 音频文件
     */
    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RenException("音频文件不能为空");
        }
        
        // 校验文件大小（最大5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RenException("音频文件大小不能超过10MB");
        }
        
        try {
            // 获取文件类型
            String fileType = FileTypeUtil.getType(file.getInputStream());
            boolean isValidType = false;
            
            for (String supportedType : SUPPORTED_AUDIO_TYPES) {
                if (supportedType.equalsIgnoreCase(fileType)) {
                    isValidType = true;
                    break;
                }
            }
            
            if (!isValidType) {
                throw new RenException("不支持的音频格式，请上传wav、mp3或m4a格式的音频文件");
            }
        } catch (Exception e) {
            log.error("校验音频文件异常", e);
            throw new RenException("音频文件校验失败");
        }
    }
}
