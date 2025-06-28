package xiaozhi.modules.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语音克隆请求DTO
 *
 * @author zjy
 * @since 2025-3-21
 */
@Data
@Schema(description = "语音克隆请求参数")
public class VoiceCloneRequestDTO {

    @Schema(description = "音频文件URL")
    private String url;
} 