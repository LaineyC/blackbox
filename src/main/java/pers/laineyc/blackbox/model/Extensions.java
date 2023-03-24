package pers.laineyc.blackbox.model;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class Extensions {

    @Valid
    private Params params;

    @Data
    public static class Params{

        private String validationScript;
    }

}
