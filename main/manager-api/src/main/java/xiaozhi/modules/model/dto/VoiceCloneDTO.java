package xiaozhi.modules.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 音色表数据DTO
 *
 * @author zjy
 * @since 2025-3-21
 */
@Data
@Schema(description = "音色表信息")
public class VoiceCloneDTO {

    @Schema(description = "语言")
    @NotBlank(message = "{timbre.languages.require}")
    private String languages;

    @Schema(description = "音色名称")
    @NotBlank(message = "{timbre.name.require}")
    private String name;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "音色编码")
    private String ttsVoice;

    @Schema(description = "音频播放地址")
    private String voiceDemo;

    @Schema(description = "被克隆的音频文件")
    @JsonIgnore
    private MultipartFile voiceFile;
}
