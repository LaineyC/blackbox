package pers.laineyc.blackbox.context;

import org.slf4j.Logger;
import io.micrometer.core.instrument.*;
import pers.laineyc.blackbox.model.Case;
import pers.laineyc.blackbox.model.Exporter;
import pers.laineyc.blackbox.strategy.monitor.MonitorStrategy;

import java.net.http.HttpClient;

public class CaseContext {

    private Exporter exporter;

    private Case case_;

    private Logger logger;

    private MeterRegistry meterRegistry;

    private ContextApi.Scope stores;

    private ContextApi.Meters meters;

    private ContextApi.Http http;

    private ContextApi.Json json;

    private ContextApi.Shell shell;

    private ContextApi.Log log;

    public CaseContext(Exporter exporter, Case case_, Logger logger, HttpClient httpClient){
        this.exporter = exporter;
        this.case_ = case_;
        this.meterRegistry = MonitorStrategy.getStrategy(case_.getMonitor()).buildMeterRegistry(exporter, case_);
        this.logger = logger;

        this.stores = new ContextApi.Scope();
        this.meters = new ContextApi.Meters(meterRegistry, exporter.getMeters());
        this.http = new ContextApi.Http(httpClient);
        this.json = new ContextApi.Json();
        this.shell = new ContextApi.Shell();
        this.log = new ContextApi.Log(logger);
    }

    public Exporter getExporter() {
        return exporter;
    }

    public Case getCase() {
        return case_;
    }

    public ContextApi.Scope getStores() {
        return stores;
    }

    public ContextApi.Meters getMeters() {
        return meters;
    }

    public ContextApi.Http getHttp() {
        return http;
    }

    public ContextApi.Json getJson() {
        return json;
    }

    public ContextApi.Shell getShell() {
        return shell;
    }

    public ContextApi.Log getLog() {
        return log;
    }

    public Logger getLogger() {
        return logger;
    }

    public String scrape() {
        return MonitorStrategy.getStrategy(case_.getMonitor()).scrape(meterRegistry);
    }

}
