package pers.laineyc.blackbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.laineyc.blackbox.strategy.PrometheusMonitorStrategy;

@Configuration
public class MonitorStrategyConfigurer {

    @Bean
    public PrometheusMonitorStrategy prometheusMonitorStrategy(){
        return new PrometheusMonitorStrategy();
    }

}
