package xiaozhi.modules.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语音克隆响应DTO
 *
 * @author zjy
 * @since 2025-3-21
 */
@Data
@Schema(description = "语音克隆响应结果")
public class VoiceCloneResponseDTO {

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "消息")
    private String msg;

    @Schema(description = "语音ID")
    private String tts_voice;
} 