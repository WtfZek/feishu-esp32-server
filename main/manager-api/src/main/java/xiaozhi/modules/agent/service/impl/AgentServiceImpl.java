package xiaozhi.modules.agent.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.AllArgsConstructor;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.service.impl.BaseServiceImpl;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.ConvertUtils;
import xiaozhi.common.utils.JsonUtils;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.dto.AgentCreateDTO;
import xiaozhi.modules.agent.dto.AgentDTO;
import xiaozhi.modules.agent.dto.AgentUpdateDTO;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.agent.entity.AgentPluginMapping;
import xiaozhi.modules.agent.entity.AgentTemplateEntity;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentPluginMappingService;
import xiaozhi.modules.agent.service.AgentService;
import xiaozhi.modules.agent.service.AgentTemplateService;
import xiaozhi.modules.agent.vo.AgentInfoVO;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.model.dto.ModelProviderDTO;
import xiaozhi.modules.model.service.ModelConfigService;
import xiaozhi.modules.model.service.ModelProviderService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.enums.SuperAdminEnum;
import xiaozhi.modules.timbre.service.TimbreService;

@Service
@AllArgsConstructor
public class AgentServiceImpl extends BaseServiceImpl<AgentDao, AgentEntity> implements AgentService {
    private final AgentDao agentDao;
    private final TimbreService timbreModelService;
    private final ModelConfigService modelConfigService;
    private final RedisUtils redisUtils;
    private final DeviceService deviceService;
    private final AgentPluginMappingService agentPluginMappingService;
    private final AgentChatHistoryService agentChatHistoryService;
    private final AgentTemplateService agentTemplateService;
    private final ModelProviderService modelProviderService;

    @Override
    public PageData<AgentEntity> adminAgentList(Map<String, Object> params) {
        IPage<AgentEntity> page = agentDao.selectPage(
                getPage(params, "agent_name", true),
                new QueryWrapper<>());
        return new PageData<>(page.getRecords(), page.getTotal());
    }

    @Override
    public AgentInfoVO getAgentById(String id) {
        AgentInfoVO agent = agentDao.selectAgentInfoById(id);

        if (agent == null) {
            throw new RenException("智能体不存在");
        }

        if (agent.getMemModelId() != null && agent.getMemModelId().equals(Constant.MEMORY_NO_MEM)) {
            agent.setChatHistoryConf(Constant.ChatHistoryConfEnum.IGNORE.getCode());
            if (agent.getChatHistoryConf() == null) {
                agent.setChatHistoryConf(Constant.ChatHistoryConfEnum.RECORD_TEXT_AUDIO.getCode());
            }
        }
        // 无需额外查询插件列表，已通过SQL查询出来
        return agent;
    }

    @Override
    public boolean insert(AgentEntity entity) {
        // 如果ID为空，自动生成一个UUID作为ID
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            entity.setId(UUID.randomUUID().toString().replace("-", ""));
        }

        // 如果智能体编码为空，自动生成一个带前缀的编码
        if (entity.getAgentCode() == null || entity.getAgentCode().trim().isEmpty()) {
            entity.setAgentCode("AGT_" + System.currentTimeMillis());
        }

        // 如果排序字段为空，设置默认值0
        if (entity.getPublished() == null) {
            entity.setPublished(0);
        }

        // 如果排序字段为空，设置默认值0
        if (entity.getSort() == null) {
            entity.setSort(0);
        }

        return super.insert(entity);
    }

    @Override
    public void deleteAgentByUserId(Long userId) {
        UpdateWrapper<AgentEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        baseDao.delete(wrapper);
    }

    @Override
    public List<AgentDTO> getUserAgents(Long userId) {
        QueryWrapper<AgentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<AgentEntity> agents = agentDao.selectList(wrapper);
        return agents.stream().map(agent -> {
            AgentDTO dto = new AgentDTO();
            dto.setId(agent.getId());
            dto.setAgentName(agent.getAgentName());
            dto.setSystemPrompt(agent.getSystemPrompt());

            // 获取 TTS 模型名称
            dto.setTtsModelName(modelConfigService.getModelNameById(agent.getTtsModelId()));

            // 获取 LLM 模型名称
            dto.setLlmModelName(modelConfigService.getModelNameById(agent.getLlmModelId()));

            // 获取 VLLM 模型名称
            dto.setVllmModelName(modelConfigService.getModelNameById(agent.getVllmModelId()));

            // 获取记忆模型名称
            dto.setMemModelId(agent.getMemModelId());

            // 获取 TTS 音色名称
            dto.setTtsVoiceName(timbreModelService.getTimbreNameById(agent.getTtsVoiceId()));

            // 获取智能体最近的最后连接时长
            dto.setLastConnectedAt(deviceService.getLatestLastConnectionTime(agent.getId()));

            // 获取设备数量
            dto.setDeviceCount(getDeviceCountByAgentId(agent.getId()));

            // 设置发布状态
            dto.setPublished(agent.getPublished());
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AgentDTO> getPublishedAgents() {
        QueryWrapper<AgentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("published", 1);
        List<AgentEntity> agents = agentDao.selectList(wrapper);
        return agents.stream().map(agent -> {
            AgentDTO dto = new AgentDTO();
            dto.setId(agent.getId());
            dto.setAgentName(agent.getAgentName());
            dto.setSystemPrompt(agent.getSystemPrompt());

            // 获取 TTS 模型名称
            dto.setTtsModelName(modelConfigService.getModelNameById(agent.getTtsModelId()));

            // 获取 LLM 模型名称
            dto.setLlmModelName(modelConfigService.getModelNameById(agent.getLlmModelId()));

            // 获取 VLLM 模型名称
            dto.setVllmModelName(modelConfigService.getModelNameById(agent.getVllmModelId()));

            // 获取记忆模型名称
            dto.setMemModelId(agent.getMemModelId());

            // 获取 TTS 音色名称
            dto.setTtsVoiceName(timbreModelService.getTimbreNameById(agent.getTtsVoiceId()));

            // 获取智能体最近的最后连接时长
            dto.setLastConnectedAt(deviceService.getLatestLastConnectionTime(agent.getId()));

            // 获取设备数量
            dto.setDeviceCount(getDeviceCountByAgentId(agent.getId()));

            // 设置发布状态
            dto.setPublished(agent.getPublished());
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public PageData<AgentDTO> getPublishedAgentsPage(Map<String, Object> params) {
        // 构建查询条件
        QueryWrapper<AgentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("published", 1);
        
        // 获取分页对象
        IPage<AgentEntity> page = agentDao.selectPage(
                getPage(params, "agent_name", true),
                wrapper);
        
        // 转换为DTO对象
        List<AgentDTO> dtoList = page.getRecords().stream().map(agent -> {
            AgentDTO dto = new AgentDTO();
            dto.setId(agent.getId());
            dto.setAgentName(agent.getAgentName());
            dto.setSystemPrompt(agent.getSystemPrompt());

            // 获取 TTS 模型名称
            dto.setTtsModelName(modelConfigService.getModelNameById(agent.getTtsModelId()));

            // 获取 LLM 模型名称
            dto.setLlmModelName(modelConfigService.getModelNameById(agent.getLlmModelId()));

            // 获取 VLLM 模型名称
            dto.setVllmModelName(modelConfigService.getModelNameById(agent.getVllmModelId()));

            // 获取记忆模型名称
            dto.setMemModelId(agent.getMemModelId());

            // 获取 TTS 音色名称
            dto.setTtsVoiceName(timbreModelService.getTimbreNameById(agent.getTtsVoiceId()));

            // 获取智能体最近的最后连接时长
            dto.setLastConnectedAt(deviceService.getLatestLastConnectionTime(agent.getId()));

            // 获取设备数量
            dto.setDeviceCount(getDeviceCountByAgentId(agent.getId()));

            // 设置发布状态
            dto.setPublished(agent.getPublished());
            
            return dto;
        }).collect(Collectors.toList());
        
        // 返回分页数据
        return new PageData<>(dtoList, page.getTotal());
    }

    // TODO 改善这个屎山代码
    @Override
    public PageData<AgentDTO> getPublishedAgentsPage(Map<String, Object> params, AgentDTO filterDto) {
        // 构建查询条件
        QueryWrapper<AgentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("published", 1);
        
        // 添加筛选条件
        if (filterDto != null) {
            // 如果agentName不为空，添加名称模糊查询条件
            if (StringUtils.isNotBlank(filterDto.getAgentName())) {
                wrapper.like("agent_name", "%" + filterDto.getAgentName() + "%");
            }
            
            // 如果ttsModelName不为空，添加语音合成模型条件
            if (StringUtils.isNotBlank(filterDto.getTtsModelName())) {
                wrapper.inSql("tts_model_id", "SELECT id FROM ai_model_config WHERE model_name = '" + filterDto.getTtsModelName() + "'");
            }
            
            // 如果llmModelName不为空，添加大语言模型条件
            if (StringUtils.isNotBlank(filterDto.getLlmModelName())) {
                wrapper.inSql("llm_model_id", "SELECT id FROM ai_model_config WHERE model_name = '" + filterDto.getLlmModelName() + "'");
            }
            
            // 如果vllmModelName不为空，添加视觉语言模型条件
            if (StringUtils.isNotBlank(filterDto.getVllmModelName())) {
                wrapper.inSql("vllm_model_id", "SELECT id FROM ai_model_config WHERE model_name = '" + filterDto.getVllmModelName() + "'");
            }
            
            // 如果ttsVoiceName不为空，添加音色条件
            if (StringUtils.isNotBlank(filterDto.getTtsVoiceName())) {
                wrapper.exists("SELECT 1 FROM ai_tts_voice v WHERE v.id = ai_agent.tts_voice_id AND v.name LIKE {0}",
                        "%" + filterDto.getTtsVoiceName() + "%");
            }
            
            // 如果systemPrompt不为空，添加角色设定模糊查询条件
            if (StringUtils.isNotBlank(filterDto.getSystemPrompt())) {
                wrapper.like("system_prompt", "%" + filterDto.getSystemPrompt() + "%");
            }
            
            // 如果memModelId不为空，添加记忆模型条件
            if (StringUtils.isNotBlank(filterDto.getMemModelId())) {
                wrapper.eq("mem_model_id", filterDto.getMemModelId());
            }
        }
        
        // 获取分页对象
        IPage<AgentEntity> page = agentDao.selectPage(
                getPage(params, "agent_name", true),
                wrapper);
        
        // 转换为DTO对象
        List<AgentDTO> dtoList = page.getRecords().stream().map(agent -> {
            AgentDTO dto = new AgentDTO();
            dto.setId(agent.getId());
            dto.setAgentName(agent.getAgentName());
            dto.setSystemPrompt(agent.getSystemPrompt());

            // 获取 TTS 模型名称
            dto.setTtsModelName(modelConfigService.getModelNameById(agent.getTtsModelId()));

            // 获取 LLM 模型名称
            dto.setLlmModelName(modelConfigService.getModelNameById(agent.getLlmModelId()));

            // 获取 VLLM 模型名称
            dto.setVllmModelName(modelConfigService.getModelNameById(agent.getVllmModelId()));

            // 获取记忆模型名称
            dto.setMemModelId(agent.getMemModelId());

            // 获取 TTS 音色名称
            dto.setTtsVoiceName(timbreModelService.getTimbreNameById(agent.getTtsVoiceId()));

            // 获取智能体最近的最后连接时长
            dto.setLastConnectedAt(deviceService.getLatestLastConnectionTime(agent.getId()));

            // 获取设备数量
            dto.setDeviceCount(getDeviceCountByAgentId(agent.getId()));

            // 设置发布状态
            dto.setPublished(agent.getPublished());
            
            return dto;
        }).collect(Collectors.toList());
        
        // 返回分页数据
        return new PageData<>(dtoList, page.getTotal());
    }

    @Override
    public Integer getDeviceCountByAgentId(String agentId) {
        if (StringUtils.isBlank(agentId)) {
            return 0;
        }

        // 先从Redis中获取
        Integer cachedCount = (Integer) redisUtils.get(RedisKeys.getAgentDeviceCountById(agentId));
        if (cachedCount != null) {
            return cachedCount;
        }

        // 如果Redis中没有，则从数据库查询
        Integer deviceCount = agentDao.getDeviceCountByAgentId(agentId);

        // 将结果存入Redis
        if (deviceCount != null) {
            redisUtils.set(RedisKeys.getAgentDeviceCountById(agentId), deviceCount, 60);
        }

        return deviceCount != null ? deviceCount : 0;
    }

    @Override
    public AgentEntity getDefaultAgentByMacAddress(String macAddress) {
        if (StringUtils.isEmpty(macAddress)) {
            return null;
        }
        return agentDao.getDefaultAgentByMacAddress(macAddress);
    }

    @Override
    public boolean checkAgentPermission(String agentId, Long userId) {
        // 获取智能体信息
        AgentEntity agent = getAgentById(agentId);
        if (agent == null) {
            return false;
        }

        // 如果是超级管理员，直接返回true
        if (SecurityUser.getUser().getSuperAdmin() == SuperAdminEnum.YES.value()) {
            return true;
        }

        // 检查是否是智能体的所有者
        return userId.equals(agent.getUserId());
    }

    // 根据id更新智能体信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAgentById(String agentId, AgentUpdateDTO dto) {
        // 先查询现有实体
        AgentEntity existingEntity = this.getAgentById(agentId);
        if (existingEntity == null) {
            throw new RuntimeException("智能体不存在");
        }

        // 只更新提供的非空字段
        if (dto.getAgentName() != null) {
            existingEntity.setAgentName(dto.getAgentName());
        }
        if (dto.getAgentCode() != null) {
            existingEntity.setAgentCode(dto.getAgentCode());
        }
        if (dto.getAsrModelId() != null) {
            existingEntity.setAsrModelId(dto.getAsrModelId());
        }
        if (dto.getVadModelId() != null) {
            existingEntity.setVadModelId(dto.getVadModelId());
        }
        if (dto.getLlmModelId() != null) {
            existingEntity.setLlmModelId(dto.getLlmModelId());
        }
        if (dto.getVllmModelId() != null) {
            existingEntity.setVllmModelId(dto.getVllmModelId());
        }
        if (dto.getTtsModelId() != null) {
            existingEntity.setTtsModelId(dto.getTtsModelId());
        }
        if (dto.getTtsVoiceId() != null) {
            existingEntity.setTtsVoiceId(dto.getTtsVoiceId());
        }
        if (dto.getMemModelId() != null) {
            existingEntity.setMemModelId(dto.getMemModelId());
        }
        if (dto.getIntentModelId() != null) {
            existingEntity.setIntentModelId(dto.getIntentModelId());
        }
        if (dto.getSystemPrompt() != null) {
            existingEntity.setSystemPrompt(dto.getSystemPrompt());
        }
        if (dto.getSummaryMemory() != null) {
            existingEntity.setSummaryMemory(dto.getSummaryMemory());
        }
        if (dto.getChatHistoryConf() != null) {
            existingEntity.setChatHistoryConf(dto.getChatHistoryConf());
        }
        if (dto.getLangCode() != null) {
            existingEntity.setLangCode(dto.getLangCode());
        }
        if (dto.getLanguage() != null) {
            existingEntity.setLanguage(dto.getLanguage());
        }
        if (dto.getPublished() != null) {
            existingEntity.setPublished(dto.getPublished());
        }
        if (dto.getSort() != null) {
            existingEntity.setSort(dto.getSort());
        }

        // 更新函数插件信息
        List<AgentUpdateDTO.FunctionInfo> functions = dto.getFunctions();
        if (functions != null) {
            // 1. 收集本次提交的 pluginId
            List<String> newPluginIds = functions.stream()
                    .map(AgentUpdateDTO.FunctionInfo::getPluginId)
                    .toList();

            // 2. 查询当前agent现有的所有映射
            List<AgentPluginMapping> existing = agentPluginMappingService.list(
                    new QueryWrapper<AgentPluginMapping>()
                            .eq("agent_id", agentId));
            Map<String, AgentPluginMapping> existMap = existing.stream()
                    .collect(Collectors.toMap(AgentPluginMapping::getPluginId, Function.identity()));

            // 3. 构造所有要 保存或更新 的实体
            List<AgentPluginMapping> allToPersist = functions.stream().map(info -> {
                AgentPluginMapping m = new AgentPluginMapping();
                m.setAgentId(agentId);
                m.setPluginId(info.getPluginId());
                m.setParamInfo(JsonUtils.toJsonString(info.getParamInfo()));
                AgentPluginMapping old = existMap.get(info.getPluginId());
                if (old != null) {
                    // 已存在，设置id表示更新
                    m.setId(old.getId());
                }
                return m;
            }).toList();

            // 4. 拆分：已有ID的走更新，无ID的走插入
            List<AgentPluginMapping> toUpdate = allToPersist.stream()
                    .filter(m -> m.getId() != null)
                    .toList();
            List<AgentPluginMapping> toInsert = allToPersist.stream()
                    .filter(m -> m.getId() == null)
                    .toList();

            if (!toUpdate.isEmpty()) {
                agentPluginMappingService.updateBatchById(toUpdate);
            }
            if (!toInsert.isEmpty()) {
                agentPluginMappingService.saveBatch(toInsert);
            }

            // 5. 删除本次不在提交列表里的插件映射
            List<Long> toDelete = existing.stream()
                    .filter(old -> !newPluginIds.contains(old.getPluginId()))
                    .map(AgentPluginMapping::getId)
                    .toList();
            if (!toDelete.isEmpty()) {
                agentPluginMappingService.removeBatchByIds(toDelete);
            }
        }

        // 设置更新者信息
        UserDetail user = SecurityUser.getUser();
        existingEntity.setUpdater(user.getId());
        existingEntity.setUpdatedAt(new Date());

        // 更新记忆策略
        if (existingEntity.getMemModelId() == null || existingEntity.getMemModelId().equals(Constant.MEMORY_NO_MEM)) {
            // 删除所有记录
            agentChatHistoryService.deleteByAgentId(existingEntity.getId(), true, true);
            existingEntity.setSummaryMemory("");
        } else if (existingEntity.getChatHistoryConf() != null && existingEntity.getChatHistoryConf() == 1) {
            // 删除音频数据
            agentChatHistoryService.deleteByAgentId(existingEntity.getId(), true, false);
        }
        this.updateById(existingEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAgent(AgentCreateDTO dto) {
        // 转换为实体
        AgentEntity entity = ConvertUtils.sourceToTarget(dto, AgentEntity.class);

        // 获取默认模板
        AgentTemplateEntity template = agentTemplateService.getDefaultTemplate();
        if (template != null) {
            // 设置模板中的默认值
            entity.setAsrModelId(template.getAsrModelId());
            entity.setVadModelId(template.getVadModelId());
            entity.setLlmModelId(template.getLlmModelId());
            entity.setVllmModelId(template.getVllmModelId());
            entity.setTtsModelId(template.getTtsModelId());
            entity.setTtsVoiceId(template.getTtsVoiceId());
            entity.setMemModelId(template.getMemModelId());
            entity.setIntentModelId(template.getIntentModelId());
            entity.setSystemPrompt(template.getSystemPrompt());
            entity.setSummaryMemory(template.getSummaryMemory());
            entity.setChatHistoryConf(template.getChatHistoryConf());
            entity.setLangCode(template.getLangCode());
            entity.setLanguage(template.getLanguage());
        }

        // 设置用户ID和创建者信息
        UserDetail user = SecurityUser.getUser();
        entity.setUserId(user.getId());
        entity.setCreator(user.getId());
        entity.setCreatedAt(new Date());
        
        // 设置默认发布状态为未发布(0)
        entity.setPublished(0);

        // 保存智能体
        insert(entity);

        // 设置默认插件
        List<AgentPluginMapping> toInsert = new ArrayList<>();
        // 播放音乐、查天气、查新闻
        String[] pluginIds = new String[] { "SYSTEM_PLUGIN_MUSIC", "SYSTEM_PLUGIN_WEATHER",
                "SYSTEM_PLUGIN_NEWS_NEWSNOW" };
        for (String pluginId : pluginIds) {
            ModelProviderDTO provider = modelProviderService.getById(pluginId);
            if (provider == null) {
                continue;
            }
            AgentPluginMapping mapping = new AgentPluginMapping();
            mapping.setPluginId(pluginId);

            Map<String, Object> paramInfo = new HashMap<>();
            List<Map<String, Object>> fields = JsonUtils.parseObject(provider.getFields(), List.class);
            if (fields != null) {
                for (Map<String, Object> field : fields) {
                    paramInfo.put((String) field.get("key"), field.get("default"));
                }
            }
            mapping.setParamInfo(JsonUtils.toJsonString(paramInfo));
            mapping.setAgentId(entity.getId());
            toInsert.add(mapping);
        }
        // 保存默认插件
        agentPluginMappingService.saveBatch(toInsert);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String copyAgent(String id) {
        // 检查ID是否为空
        if (StringUtils.isBlank(id)) {
            throw new RenException("智能体ID不能为空");
        }

        // 获取原智能体信息
        AgentInfoVO sourceAgent = this.getAgentById(id);
        if (sourceAgent == null) {
            throw new RenException("智能体不存在");
        }

        if (sourceAgent.getPublished() == null || sourceAgent.getPublished() != 1) {
            throw new RenException("智能体未发布，无法复制");
        }

        // 创建新的智能体创建DTO
        AgentCreateDTO agentCreateDTO = new AgentCreateDTO();

        // 设置智能体名称为原名称加上"创意中心"
        agentCreateDTO.setAgentName(sourceAgent.getAgentName() + "——创意中心");

        // 创建新的智能体（此时会使用当前用户作为creator）
        String newAgentId = this.createAgent(agentCreateDTO);

        // 更新新智能体的配置，复制原智能体的所有配置信息
        AgentUpdateDTO updateDTO = ConvertUtils.sourceToTarget(sourceAgent, AgentUpdateDTO.class);
        // 保留新名称
        updateDTO.setAgentName(agentCreateDTO.getAgentName());
        // 重置为未发布
        updateDTO.setPublished(0);
        // 更新配置
        this.updateAgentById(newAgentId, updateDTO);

        // 先删除createAgent方法中自动添加的默认插件
        agentPluginMappingService.remove(
                new QueryWrapper<AgentPluginMapping>().eq("agent_id", newAgentId)
        );

        // 复制插件配置
        List<?> originalPlugins = sourceAgent.getFunctions();
        if (originalPlugins != null && !originalPlugins.isEmpty()) {
            List<AgentPluginMapping> newPlugins = new ArrayList<>();

            // 处理每个插件映射
            for (Object obj : originalPlugins) {
                // 处理LinkedHashMap到AgentPluginMapping的转换
                AgentPluginMapping newPlugin = new AgentPluginMapping();

                if (obj instanceof Map<?, ?> map) {
                    // 从Map中提取值
                    if (map.get("pluginId") != null) {
                        newPlugin.setPluginId(String.valueOf(map.get("pluginId")));
                    }
                    if (map.get("paramInfo") != null) {
                        Object paramInfoObj = map.get("paramInfo");
                        // 如果paramInfo是字符串，直接使用
                        if (paramInfoObj instanceof String) {
                            newPlugin.setParamInfo((String) paramInfoObj);
                        }
                        // 如果paramInfo是Map，转换为JSON字符串
                        else if (paramInfoObj instanceof Map) {
                            newPlugin.setParamInfo(JsonUtils.toJsonString(paramInfoObj));
                        }
                        // 其他情况，转换为字符串
                        else {
                            newPlugin.setParamInfo(String.valueOf(paramInfoObj));
                        }
                    }
                    // ID设为null确保创建新记录
                    newPlugin.setId(null);
                    newPlugin.setAgentId(newAgentId);
                    newPlugins.add(newPlugin);
                }
            }

            // 保存新的插件映射
            if (!newPlugins.isEmpty()) {
                agentPluginMappingService.saveBatch(newPlugins);
            }
        }

        return newAgentId;
    }

}
