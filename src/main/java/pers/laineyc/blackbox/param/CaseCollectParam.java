package pers.laineyc.blackbox.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaseCollectParam {

    @NotBlank(message = "name不能为空")
    private String name;

}
