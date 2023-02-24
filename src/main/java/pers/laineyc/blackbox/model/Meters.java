package pers.laineyc.blackbox.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pers.laineyc.blackbox.model.meter.Counter;
import pers.laineyc.blackbox.model.meter.Summary;
import pers.laineyc.blackbox.model.meter.Gauge;
import pers.laineyc.blackbox.model.meter.Timer;

import java.util.ArrayList;
import java.util.List;

@Data
public class Meters {

    @Valid
    private List<@NotNull(message = "Meters.counters元素不能为空") Counter> counters = new ArrayList<>();

    @Valid
    private List<@NotNull(message = "Meters.gauges元素不能为空") Gauge> gauges = new ArrayList<>();

    @Valid
    private List<@NotNull(message = "Meters.timers元素不能为空") Timer> timers = new ArrayList<>();

    @Valid
    private List<@NotNull(message = "Meters.summaries元素不能为空") Summary> summaries = new ArrayList<>();

}

