package pers.laineyc.blackbox.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pers.laineyc.blackbox.enums.Event;
import pers.laineyc.blackbox.enums.Time;

@Data
public class Trigger {

    @NotNull(message = "Trigger.time不能为空")
    private Time time;

    @NotNull(message = "Trigger.event不能为空")
    private Event event;

    private String pipeline;

    private String script;
}
