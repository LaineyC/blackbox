package pers.laineyc.blackbox.config;

import org.springframework.context.annotation.Configuration;
import pers.laineyc.blackbox.strategy.monitor.PrometheusMonitorStrategy;

@Configuration
public class MonitorStrategyConfigurer {

    static {
        new PrometheusMonitorStrategy();
    }

}
