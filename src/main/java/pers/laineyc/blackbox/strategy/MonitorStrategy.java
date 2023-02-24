package pers.laineyc.blackbox.strategy;

import io.micrometer.core.instrument.MeterRegistry;
import pers.laineyc.blackbox.enums.Monitor;
import pers.laineyc.blackbox.model.Case;
import pers.laineyc.blackbox.model.Exporter;
import java.util.HashMap;
import java.util.Map;

public abstract class MonitorStrategy {

    private static final Map<Monitor, MonitorStrategy> MONITOR_HANDLER_MAP = new HashMap<>();

    public static MonitorStrategy getStrategy(Monitor monitor){
        return MONITOR_HANDLER_MAP.get(monitor);
    }

    protected void register(Monitor monitor){
        MONITOR_HANDLER_MAP.put(monitor, this);
    }

    public abstract MeterRegistry buildMeterRegistry(Exporter exporter, Case case_);

    public abstract String scrape(MeterRegistry meterRegistry);

}
