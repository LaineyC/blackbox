package pers.laineyc.blackbox.context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import pers.laineyc.blackbox.model.meter.Meter;
import pers.laineyc.blackbox.model.meter.Summary;
import pers.laineyc.blackbox.util.ShellUtil;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class ContextApi {

    public static class Meters{

        private final Map<String, Meter> meterCache;

        private final MeterRegistry meterRegistry;

        public Meters(MeterRegistry meterRegistry, pers.laineyc.blackbox.model.Meters meters) {
            this.meterRegistry = meterRegistry;

            List<pers.laineyc.blackbox.model.meter.Counter> counters = meters.getCounters();
            Map<String, Meter> meterCache = new HashMap<>();
            for(Meter meter : counters){
                meterCache.put(meter.getName(), meter);
            }
            List<pers.laineyc.blackbox.model.meter.Gauge> gauges = meters.getGauges();
            for(Meter meter : gauges){
                meterCache.put(meter.getName(), meter);
            }
            List<pers.laineyc.blackbox.model.meter.Timer> times = meters.getTimers();
            for(Meter meter : times){
                meterCache.put(meter.getName(), meter);
            }
            List<pers.laineyc.blackbox.model.meter.Summary> summaries = meters.getSummaries();
            for(Meter meter : summaries){
                meterCache.put(meter.getName(), meter);
            }

            this.meterCache = meterCache;
        }

        public void counter(Map<String, Object> args){
            String meter = (String)args.get("meter");
            List<String> labelValues = (List<String>)args.get("tags");
            Number value = (Number)args.get("value");
            counter(meter, labelValues, value.doubleValue());
        }

        public void gauge(Map<String, Object> args){
            String meter = (String)args.get("meter");
            List<String> labelValues = (List<String>)args.get("tags");
            Number value = (Number)args.get("value");
            gauge(meter, labelValues, value.doubleValue());
        }

        public void timer(Map<String, Object> args){
            String meter = (String)args.get("meter");
            List<String> labelValues = (List<String>)args.get("tags");
            Number value = (Number)args.get("value");
            timer(meter, labelValues, value.longValue());
        }

        public void summary(Map<String, Object> args){
            String meter = (String)args.get("meter");
            List<String> labelValues = (List<String>)args.get("tags");
            Number value = (Number)args.get("value");
            summary(meter, labelValues, value.doubleValue());
        }

        private static Duration toNanos(Number value, TimeUnit timeUnit){
            return Duration.ofNanos(timeUnit.toNanos(value.longValue()));
        }

        private void counter(String name, List<String> labelValues, Double increment){
            Meter meter = meterCache.get(name);
            String unit = meter.getUnit();
            String desc = meter.getDesc();
            List<Tag> tags = buildTags(name, labelValues);
            Counter.builder(name)
                    .baseUnit(unit)
                    .description(desc)
                    .tags(tags)
                    .register(meterRegistry)
                    .increment(increment);
        }

        private void gauge(String name, List<String> labelValues, Double value){
            Meter meter = meterCache.get(name);
            String unit = meter.getUnit();
            String desc = meter.getDesc();
            List<Tag> tags = buildTags(name, labelValues);
            Gauge.builder(name, () -> value)
                    .baseUnit(unit)
                    .description(desc)
                    .tags(tags)
                    .register(meterRegistry);
        }

        private void timer(String name, List<String> labelValues, Long duration){
            Meter meter = meterCache.get(name);
            String desc = meter.getDesc();
            List<Tag> tags = buildTags(name, labelValues);
            pers.laineyc.blackbox.model.meter.Timer timer = (pers.laineyc.blackbox.model.meter.Timer) meter;

            TimeUnit timeUnit = timer.getTimeUnit();

            Timer.Builder builder = Timer.builder(name)
                    .description(desc)
                    .tags(tags);

            Double minimumExpectedValue = timer.getMinimumExpectedValue();
            if(minimumExpectedValue != null) {
                builder.minimumExpectedValue(toNanos(minimumExpectedValue, timeUnit));
            }

            Double maximumExpectedValue = timer.getMaximumExpectedValue();
            if(maximumExpectedValue != null) {
                builder.maximumExpectedValue(toNanos(maximumExpectedValue, timeUnit));
            }

            double[] percentiles = timer.getPercentiles();
            if(maximumExpectedValue != null) {
                builder.publishPercentiles(percentiles);
            }

            Boolean percentileHistogram = timer.getPercentileHistogram();
            if(percentileHistogram != null) {
                builder.publishPercentileHistogram(percentileHistogram);
            }

            Integer percentilePrecision = timer.getPercentilePrecision();
            if(percentilePrecision != null) {
                builder.percentilePrecision(percentilePrecision);
            }

            Integer bufferLength = timer.getBufferLength();
            if(bufferLength != null) {
                builder.distributionStatisticBufferLength(bufferLength);
            }

            Double expiry = timer.getExpiry();
            if(expiry != null) {
                builder.distributionStatisticExpiry(toNanos(expiry, timeUnit));
            }

            double[] serviceLevelObjectives = timer.getServiceLevelObjectives();
            if(serviceLevelObjectives != null) {
                Duration[] durations = new Duration[serviceLevelObjectives.length];
                for(int i = 0, n = serviceLevelObjectives.length; i < n; i++) {
                    durations[i] = toNanos(serviceLevelObjectives[i], timeUnit);
                }
                builder.serviceLevelObjectives(durations);
            }

            builder
                    .register(meterRegistry)
                    .record(duration, timeUnit);
        }

        private void summary(String name, List<String> labelValues, Double amount){
            Meter meter = meterCache.get(name);
            String unit = meter.getUnit();
            String desc = meter.getDesc();
            List<Tag> tags = buildTags(name, labelValues);
            Summary summary = (Summary)meter;

            TimeUnit timeUnit = summary.getTimeUnit();

            DistributionSummary.Builder builder = DistributionSummary.builder(name)
                    .baseUnit(unit)
                    .description(desc)
                    .tags(tags);

            Double scale = summary.getScale();
            if(scale != null) {
                builder.scale(scale);
            }

            Double minimumExpectedValue = summary.getMinimumExpectedValue();
            if(minimumExpectedValue != null) {
                builder.minimumExpectedValue(minimumExpectedValue);
            }

            Double maximumExpectedValue = summary.getMaximumExpectedValue();
            if(maximumExpectedValue != null) {
                builder.maximumExpectedValue(maximumExpectedValue);
            }

            double[] percentiles = summary.getPercentiles();
            if(maximumExpectedValue != null) {
                builder.publishPercentiles(percentiles);
            }

            Boolean percentileHistogram = summary.getPercentileHistogram();
            if(percentileHistogram != null) {
                builder.publishPercentileHistogram(percentileHistogram);
            }

            double[] serviceLevelObjectives = summary.getServiceLevelObjectives();
            if(serviceLevelObjectives != null) {
                builder.serviceLevelObjectives(serviceLevelObjectives);
            }

            Integer percentilePrecision = summary.getPercentilePrecision();
            if(percentilePrecision != null) {
                builder.percentilePrecision(percentilePrecision);
            }

            Integer bufferLength = summary.getBufferLength();
            if(bufferLength != null) {
                builder.distributionStatisticBufferLength(bufferLength);
            }

            Double expiry = summary.getExpiry();
            if(expiry != null) {
                builder.distributionStatisticExpiry(toNanos(expiry, timeUnit));
            }

            builder
                    .register(meterRegistry)
                    .record(amount);
        }

        private List<Tag> buildTags(String name, List<String> labelValues){
            Meter meter = meterCache.get(name);
            List<String> tagNames = meter.getTags();
            List<Tag> tags = new ArrayList<>();
            int i = 0;
            for(String tagName : tagNames) {
                tags.add(Tag.of(tagName, labelValues.get(i)));
                i++;
            }

            return tags;
        }
    }

    public static class Json {

        public Object parse(String json){
            return JSONObject.parse(json);
        }

        public String string(Object obj){
            return JSONObject.toJSONString(obj);
        }

    }

    public static class Log{
        private final Logger logger;

        public Log(Logger logger) {
            this.logger = logger;
        }

        public boolean isTrace(){
            return logger.isTraceEnabled();
        }

        public void trace(Object format, Object... arguments){
            logger.trace(buildFormat(format), arguments);
        }

        public boolean isDebug(){
            return logger.isDebugEnabled();
        }

        public void debug(Object format, Object... arguments){
            logger.debug(buildFormat(format), arguments);
        }

        public boolean isInfo(){
            return logger.isInfoEnabled();
        }

        public void info(Object format, Object... arguments){
            logger.info(buildFormat(format), arguments);
        }

        public boolean isWarn(){
            return logger.isWarnEnabled();
        }

        public void warn(Object format, Object... arguments){
            logger.warn(buildFormat(format), arguments);
        }

        public boolean isError(){
            return logger.isErrorEnabled();
        }

        public void error(Object format, Object... arguments){
            logger.error(buildFormat(format), arguments);
        }

        private static String buildFormat(Object format){
            if(format == null){
                return "null";
            }

            if(format instanceof String) {
                return (String)format;
            }

            if(format instanceof Throwable) {
                return ((Throwable)format).getMessage();
            }

            return JSON.toJSONString(format);
        }
    }

    public static class Scope {
        private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();

        public void set(String key, Object value){
            data.put(key, value);
        }

        public Object get(String key){
            return data.get(key);
        }

        public Object remove(String key){
            return data.remove(key);
        }
    }

    @Slf4j
    public static class Http {
        private final HttpClient client;

        public Http(HttpClient client) {
            this.client = client;
        }

        public HttpClient getClient() {
            return client;
        }

        public String encode(String str) {
            return URLEncoder.encode(str, StandardCharsets.UTF_8);
        }

        public String decode(String str) {
            return URLDecoder.decode(str, StandardCharsets.UTF_8);
        }

        public Map<String, Object> send(Map<String, Object> args) throws Exception {
            Number timeout = (Number)args.get("timeout");
            String method = (String)args.get("method");
            String url = (String)args.get("url");
            Map<String, Object> paramsObject = (Map<String, Object>)args.get("params");
            Map<String, Object> headersObject = (Map<String, Object>)args.get("headers");

            Map<String, List<String>> params = new HashMap<>();
            if(paramsObject != null) {
                paramsObject.forEach((k, v) -> {
                    if(v instanceof String) {
                        params.put(k, List.of((String)v));
                    }
                    else if(v instanceof List) {
                        params.put(k, (List<String>)v);
                    }
                });
            }
            Map<String, List<String>> headers = new HashMap<>();
            if(headersObject != null) {
                headersObject.forEach((k, v) -> {
                    if(v instanceof String) {
                        headers.put(k, List.of((String)v));
                    }
                    else if(v instanceof List) {
                        headers.put(k, (List<String>)v);
                    }
                });
            }
            String body = (String)args.get("body");

            Response httpResponse = exec(new Request(timeout, method, url, params, headers, body));

            Map<String, Object> returnValue = new HashMap<>();
            returnValue.put("code", httpResponse.code());
            returnValue.put("headers", httpResponse.headers());
            returnValue.put("body", httpResponse.body());
            return returnValue;
        }

        private Response exec(Request request) throws Exception {
            String method = request.method();
            if(!StringUtils.hasText(method)) {
                method = "GET";
            }
            Number timeout = request.timeout();
            String url = request.url();
            Map<String, List<String>> params = request.params();
            Map<String, List<String>> headers = request.headers();
            String body = request.body();

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url);
            if(params != null) {
                params.forEach((k, v) -> {
                    if(!CollectionUtils.isEmpty(v)) {
                        uriComponentsBuilder.queryParam(k,  v);
                    }
                    else{
                        uriComponentsBuilder.queryParam(k,  "");
                    }
                });
            }

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            if(timeout != null) {
                httpRequestBuilder.timeout(Duration.ofSeconds(timeout.longValue()));
            }

            URI uri = uriComponentsBuilder.build().toUri();
            httpRequestBuilder.uri(uri);

            if(headers != null) {
                headers.forEach((k, v) -> {
                    if(!CollectionUtils.isEmpty(v)) {
                        for(String hv : v) {
                            httpRequestBuilder.header(k,  hv);
                        }
                    }
                    else {
                        httpRequestBuilder.header(k, "");
                    }

                });
            }

            if(body != null) {
                httpRequestBuilder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            }
            else{
                httpRequestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpRequest httpRequest = httpRequestBuilder.build();
            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return new Response(httpResponse.statusCode(), httpResponse.headers().map(), httpResponse.body());
        }

        public record Request(Number timeout, String method, String url, Map<String, List<String>> params, Map<String, List<String>> headers, String body){

        }

        public record Response(int code, Map<String, List<String>> headers, String body){

        }
    }

    public static class Shell{

       public Map<String, Object> exec(Map<String, Object> args) {
           String cmd = (String)args.get("cmd");

           ShellUtil.CommandResult commandResult = ShellUtil.exec(new ShellUtil.Command(cmd));

           Map<String, Object> returnValue = new HashMap<>();
           returnValue.put("code", commandResult.code());
           returnValue.put("message", commandResult.message());
           returnValue.put("stdout", commandResult.stdout());
           returnValue.put("stderr", commandResult.stderr());
           return returnValue;
       }

    }

}
