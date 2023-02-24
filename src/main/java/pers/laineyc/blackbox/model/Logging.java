package pers.laineyc.blackbox.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.enums.LoggLevel;

@Data
public class Logging {
    @NotNull(message = "Logging.level不能为空")
    private LoggLevel level = LoggLevel.WARN;

    @Pattern(regexp = "[1-9][0-9]*(KB|MB|GB)", message = "Logging.maxFileSize 格式有误 单位KB|MB|GB")
    @NotBlank(message = "Logging.maxFileSize不能为空")
    private String maxFileSize = "100MB";

    @Pattern(regexp = "[1-9][0-9]*(KB|MB|GB)", message = "Logging.totalSizeCap 格式有误 单位KB|MB|GB")
    @NotBlank(message = "Logging.totalSizeCap不能为空")
    private String totalSizeCap = "1GB";

    @NotNull(message = "Logging.maxHistory不能为空")
    private Integer maxHistory = 30;

}
