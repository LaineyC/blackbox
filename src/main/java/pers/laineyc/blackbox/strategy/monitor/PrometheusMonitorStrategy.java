package pers.laineyc.blackbox.strategy.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import pers.laineyc.blackbox.enums.Monitor;
import pers.laineyc.blackbox.model.Case;
import pers.laineyc.blackbox.model.Exporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrometheusMonitorStrategy extends MonitorStrategy {

    public PrometheusMonitorStrategy() {
        register(Monitor.PROMETHEUS);
    }

    @Override
    public MeterRegistry buildMeterRegistry(Exporter exporter, Case case_) {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Map<String, String> commonTags = case_.getCommonTags();
        List<Tag> tags = new ArrayList<>();
        commonTags.forEach((k, v) -> tags.add(Tag.of(k, v)));
        registry.config().commonTags(tags);
        return registry;
    }

    @Override
    public String scrape(MeterRegistry meterRegistry) {
        return ((PrometheusMeterRegistry)meterRegistry).scrape();
    }

}
