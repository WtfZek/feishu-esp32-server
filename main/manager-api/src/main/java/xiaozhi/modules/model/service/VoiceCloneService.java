package xiaozhi.modules.model.service;

import xiaozhi.common.utils.Result;
import xiaozhi.modules.model.dto.VoiceCloneDTO;

import java.io.File;

/**
 * 语音克隆服务接口
 *
 * @author zjy
 * @since 2025-3-21
 */
public interface VoiceCloneService {

    /**
     * 克隆语音
     *
     * @param dto 语音克隆参数
     * @return 克隆结果
     */
    Result<?> cloneVoice(VoiceCloneDTO dto);
    
    /**
     * 从文件处理语音克隆任务（支付成功后调用）
     * 
     * @param name 音色名称
     * @param languages 语言
     * @param remark 备注
     * @param audioFile 音频文件
     * @param userId 用户ID
     * @return 处理结果
     */
    Result<?> processCloneFromFile(String name, String languages, String remark, File audioFile, Long userId);
}
