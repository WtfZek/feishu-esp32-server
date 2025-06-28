package xiaozhi.modules.model.dto;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "音色信息")
public class VoiceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "音色ID")
    private String id;

    @Schema(description = "音色名称")
    private String name;

    @Schema(description = "音色语言")
    private String languages;

    @Schema(description = "音色试听链接")
    private String voiceDemo;

    @Schema(description = "音色备注")
    private String voiceType;

    @Schema(description = "创建时间")
    private Date createDate;
}