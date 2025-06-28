package xiaozhi.modules.model.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.modules.model.dto.VoiceCloneRequestDTO;
import xiaozhi.modules.model.dto.VoiceCloneResponseDTO;

/**
 * 语音克隆服务客户端（使用WebClient实现）
 *
 * @author zjy
 * @since 2025-3-21
 */
@Slf4j
@Component
public class VoiceCloneClient {

    private final WebClient voiceCloneWebClient;

    public VoiceCloneClient(WebClient voiceCloneWebClient) {
        this.voiceCloneWebClient = voiceCloneWebClient;
    }

    /**
     * 克隆语音
     *
     * @param request 包含音频URL的请求对象
     * @return 语音克隆结果
     */
    public VoiceCloneResponseDTO cloneVoice(VoiceCloneRequestDTO request) {
        log.info("调用语音克隆API，请求参数: {}", request);
        
        try {
            return voiceCloneWebClient.post()
                .uri("/train_audio")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VoiceCloneResponseDTO.class)
                .block(); // 使用阻塞方式处理，适应原有同步代码
        } catch (Exception e) {
            log.error("语音克隆API调用异常", e);
            VoiceCloneResponseDTO errorResponse = new VoiceCloneResponseDTO();
            errorResponse.setCode(500);
            errorResponse.setMsg("语音克隆服务调用失败: " + e.getMessage());
            return errorResponse;
        }
    }
} 