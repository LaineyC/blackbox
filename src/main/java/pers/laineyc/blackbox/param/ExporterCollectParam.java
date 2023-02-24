package pers.laineyc.blackbox.param;

import com.alibaba.fastjson.annotation.JSONField;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pers.laineyc.blackbox.model.Case;

@Data
public class ExporterCollectParam {

    @JSONField(name = "case")
    @NotNull(message = "case不能为空")
    private Case case_;

}
