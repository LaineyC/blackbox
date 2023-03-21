# Demo示例
假定配置根目录rootPath为/config

## 1.Exporter设计
```text
/config
    /exporters
        /demo-exporter
            exporter.yml
            /scripts
                after.js
                before.js
                counter_metric.js
                counter_metric2.js
                gauges.js
                summaries.js
                timers.js
    /cases
        /demo-exporter-case
            case.yml
            /logs
```

**exporter.yml**
```yaml
name: demo-exporter
monitors:
  - PROMETHEUS
params:
  - name: serverUrl
    value: null
    type: STRING
  - name: username
    value: null
    type: STRING
  - name: password
    value: null
    type: STRING
  - name: account
    value: null
    type: OBJECT
    nest:
      - name: username
        value: null
        type: STRING
      - name: password
        value: null
        type: STRING
meters:
  counters:
    - name: counter.metric
      desc: counter metric
      unit:
      tags:
        - tag1
        - tag2
    - name: counter.metric2
      desc: counter metric2
      unit: second
      tags:
        - tag1
        - tag2
  gauges:
    - name: gauge.metric
      desc: gauge metric
      unit:
      tags:
        - tag1
        - tag2
    - name: gauge.metric2
      desc: gauge metric2
      unit:
      tags:
        - tag1
        - tag2
  timers:
    - name: timer.metric
      desc: timer metric
      unit:
      tags:
        - tag1
        - tag2
      percentileHistogram:
      percentiles:
      serviceLevelObjectives:
      minimumExpectedValue:
      maximumExpectedValue:
      percentilePrecision:
      expiry:
      bufferLength:
      timeUnit: SECONDS
    - name: timer.histogram.metric
      desc: timer histogram metric
      unit:
      tags:
        - tag1
        - tag2
      percentileHistogram: true
      percentiles: [0.5, 0.95, 0.99]
      serviceLevelObjectives: [500, 800, 900, 950, 990, 1000]
      minimumExpectedValue: 1
      maximumExpectedValue: 1000
      percentilePrecision:
      expiry:
      bufferLength:
      timeUnit: MILLISECONDS
  summaries:
    - name: summary.metric
      desc: summary metric
      unit:
      tags:
        - tag1
        - tag2
      scale:
      percentileHistogram:
      percentiles: [0.5, 0.95, 0.99]
      serviceLevelObjectives:
      minimumExpectedValue: 1
      maximumExpectedValue: 1000
      percentilePrecision:
      expiry:
      bufferLength:
      timeUnit: MILLISECONDS
    - name: summary.histogram.metric
      desc: summary histogram metric
      unit:
      tags:
        - tag1
        - tag2
      scale:
      percentileHistogram: true
      percentiles:
      serviceLevelObjectives: [500, 800, 900, 950, 990, 1000]
      minimumExpectedValue: 500
      maximumExpectedValue: 1100
      percentilePrecision:
      expiry:
      bufferLength:
      timeUnit: MILLISECONDS
scripts:
  - name: before_js
    file: before.js
  - name: counter_metric_js
    file: counter_metric.js
  - name: counter_metric2_js
    file: counter_metric2.js
  - name: gauges_js
    file: gauges.js
  - name: timers_js
    file: timers.js
  - name: summaries_js
    file: summaries.js
  - name: after_js
    file: after.js
pipelines:
  - name: before_pipeline
    stages:
      - title: 前置流程
        serial:
          - before_js
  - name: counters_pipeline
    stages:
      - title: 抓取counter指标
        parallel:
          - counter_metric_js
          - counter_metric2_js
  - name: gauges_pipeline
    stages:
      - title: 抓取gauge指标
        serial:
          - gauges_js
  - name: timers_pipeline
    stages:
      - title: 抓取timer指标
        serial:
          - timers_js
  - name: summaries_pipeline
    stages:
      - title: 抓取summary指标
        serial:
          - summaries_js
  - name: after_pipeline
    stages:
      - title: 后置流程
        serial:
          - after_js
triggers:
  - time: BEFORE
    event: COLLECT
    #pipeline: before_pipeline
    script: before_js
  - time: AFTER
    event: COLLECT
    pipeline: after_pipeline
collectors:
  - name: counters_collector
    pipeline: counters_pipeline
  - name: gauges_collector
    pipeline: gauges_pipeline
  - name: timers_collector
    cron: 0/30 * * * * ?
    pipeline: timers_pipeline
  - name: summaries_collector
    pipeline: summaries_pipeline
```

**before.js**
```js
// 匿名函数 可以return
(function($){
    // 打印日志
    $.log.info("before");

    // 判断是否初始化
    if($.stores.get("isInit") != null){
        return;
    }

    // 设置 是否初始化
    $.stores.set("isInit", true);

    // 设置 add函数
    $.stores.set("add", function(a, b){
        return a + b;
    });
})($);
```

**counter_metric.js**
```js
$.log.info("counter_metric");

// counter 指标
$.meters.counter({meter:"counter.metric", tags:["t1", "t2"], value:1});

// 打印参数
$.log.info($.params.serverUrl);
$.log.info($.params.username);

try{
    // URL编码
    let username = $.http.encode($.params.username);
    // http访问
    let response = $.http.send({url: $.params.serverUrl, params:{search: username}});
    $.log.info(response);
}
catch(e){
    $.log.error(e);
}

// 获取add函数
let addFunc = $.stores.get("add");
$.log.info("add(1,2) = " + addFunc(1, 2));

// 调用cmd
let result = $.shell.exec({cmd: "ipconfig"});
$.log.info(result);
```

**counter_metric2.js**
```js
$.log.info("counter_metric2");

// counter 指标
$.meters.counter({meter: "counter.metric2", tags:["t1", "t2"], value:2});
```

**gauges.js**
```js
$.log.info("gauges");

// gauge 指标
$.meters.gauge({meter:"gauge.metric", tags: ["t1", "t2"], value:3});
$.meters.gauge({meter:"gauge.metric2", tags: ["t1", "t2"], value:4});
```

**summaries.js**
```js
$.log.info("summaries");

// summary 指标
$.meters.summary({meter:"summary.metric", tags:["t1", "t2"], value:5});
$.meters.summary({meter:"summary.histogram.metric", tags:["t1", "t2"], value:5});
```

**timers.js**
```js
$.log.info("timers");

// timer 指标
$.meters.timer({meter:"timer.metric", tags:["t1", "t2"], value:5});
$.meters.timer({meter:"timer.histogram.metric", tags:["t1", "t2"], value:5});
```

**after.js**
```js
$.log.info("after");
```

## 2.调试Exporter

POST /api/v1/exporter/collect

RequestHeader
* Content-Type: application/json;charset=UTF-8

RequestBody
```json
{
  "case": {
    "name": "demo-exporter-case",
    "exporter": "demo-exporter",
    "monitor": "PROMETHEUS",
    "params": {
      "serverUrl": "http://blog.ccvv.icu",
      "username": "username",
      "password": "password",
      "account": {
        "username": "account.username",
        "password": "account.password"
      }
    },
    "commonTags": {
      "env": "test"
    },
    "collectors": [
      "counters_collector",
      "gauges_collector",
      "timers_collector",
      "summaries_collector"
    ],
    "logging": {
      "level": "ALL"
    }
  }
}
```

ResponseData

* 第一部分 Collect Info
* 第二部分 Exporter Yaml
* 第三部分 Case Yaml
* 第四部分 Metric Result

```text
23:09:47.240 [http-nio-8030-exec-2    ] INFO  -Collect start
23:09:47.241 [http-nio-8030-exec-2    ] INFO  -Trigger BEFORE COLLECT start
23:09:47.241 [http-nio-8030-exec-2    ] INFO  -----Trigger BEFORE COLLECT Script[before_js] start
23:09:48.173 [http-nio-8030-exec-2    ] INFO  -before
23:09:48.179 [http-nio-8030-exec-2    ] INFO  -----Trigger BEFORE COLLECT Script[before_js] end
23:09:48.179 [http-nio-8030-exec-2    ] INFO  -Trigger BEFORE COLLECT end
23:09:48.179 [http-nio-8030-exec-2    ] INFO  -Collector collect start
23:09:48.180 [collector-pool-thread-1 ] INFO  -----Collector[counters_collector] start
23:09:48.180 [collector-pool-thread-1 ] INFO  ---------Pipeline[counters_pipeline] start
23:09:48.180 [collector-pool-thread-1 ] INFO  ---------Pipeline[counters_pipeline] Stage[抓取counter指标] start
23:09:48.181 [collector-pool-thread-2 ] INFO  -----Collector[gauges_collector] start
23:09:48.181 [collector-pool-thread-2 ] INFO  ---------Pipeline[gauges_pipeline] start
23:09:48.181 [collector-pool-thread-3 ] INFO  -----Collector[timers_collector] start
23:09:48.181 [collector-pool-thread-2 ] INFO  ---------Pipeline[gauges_pipeline] Stage[抓取gauge指标] start
23:09:48.181 [collector-pool-thread-3 ] INFO  ---------Pipeline[timers_pipeline] start
23:09:48.181 [collector-pool-thread-3 ] INFO  ---------Pipeline[timers_pipeline] Stage[抓取timer指标] start
23:09:48.181 [collector-pool-thread-2 ] INFO  ---------Pipeline[gauges_pipeline] Stage[抓取gauge指标] serial Script[gauges_js] start
23:09:48.181 [collector-pool-thread-4 ] INFO  -----Collector[summaries_collector] start
23:09:48.181 [collector-pool-thread-3 ] INFO  ---------Pipeline[timers_pipeline] Stage[抓取timer指标] serial Script[timers_js] start
23:09:48.182 [collector-pool-thread-4 ] INFO  ---------Pipeline[summaries_pipeline] start
23:09:48.182 [collector-pool-thread-4 ] INFO  ---------Pipeline[summaries_pipeline] Stage[抓取summary指标] start
23:09:48.182 [collector-pool-thread-4 ] INFO  ---------Pipeline[summaries_pipeline] Stage[抓取summary指标] serial Script[summaries_js] start
23:09:48.182 [pipeline-pool-thread-1  ] INFO  ---------Pipeline[counters_pipeline] Stage[抓取counter指标] parallel Script[counter_metric_js] start
23:09:48.183 [pipeline-pool-thread-2  ] INFO  ---------Pipeline[counters_pipeline] Stage[抓取counter指标] parallel Script[counter_metric2_js] start
23:09:48.202 [collector-pool-thread-4 ] INFO  -summaries
23:09:48.202 [pipeline-pool-thread-2  ] INFO  -counter_metric2
23:09:48.202 [collector-pool-thread-3 ] INFO  -timers
23:09:48.202 [collector-pool-thread-2 ] INFO  -gauges
23:09:48.211 [pipeline-pool-thread-1  ] INFO  -counter_metric
23:09:48.228 [pipeline-pool-thread-1  ] INFO  -http://blog.ccvv.icu
23:09:48.229 [pipeline-pool-thread-1  ] INFO  -username
23:09:48.252 [collector-pool-thread-2 ] INFO  ---------Pipeline[gauges_pipeline] Stage[抓取gauge指标] serial Script[gauges_js] end
23:09:48.252 [collector-pool-thread-2 ] INFO  ---------Pipeline[gauges_pipeline] end
23:09:48.257 [collector-pool-thread-4 ] INFO  ---------Pipeline[summaries_pipeline] Stage[抓取summary指标] serial Script[summaries_js] end
23:09:48.257 [collector-pool-thread-4 ] INFO  ---------Pipeline[summaries_pipeline] end
23:09:48.260 [collector-pool-thread-3 ] INFO  ---------Pipeline[timers_pipeline] Stage[抓取timer指标] serial Script[timers_js] end
23:09:48.260 [collector-pool-thread-3 ] INFO  ---------Pipeline[timers_pipeline] end
23:09:48.326 [pipeline-pool-thread-1  ] INFO  -{"headers":{"accept-ranges":["bytes"],"connection":["keep-alive"],"content-length":["615"],"content-type":["text/html"],"date":["Sun, 25 Dec 2022 15:09:51 GMT"],"etag":["\"61f0168e-267\""],"last-modified":["Tue, 25 Jan 2022 15:26:06 GMT"],"server":["nginx/1.21.6"]},"code":200,"body":"<!DOCTYPE html>\n<html>\n<head>\n<title>Welcome to nginx!</title>\n<style>\nhtml { color-scheme: light dark; }\nbody { width: 35em; margin: 0 auto;\nfont-family: Tahoma, Verdana, Arial, sans-serif; }\n</style>\n</head>\n<body>\n<h1>Welcome to nginx!</h1>\n<p>If you see this page, the nginx web server is successfully installed and\nworking. Further configuration is required.</p>\n\n<p>For online documentation and support please refer to\n<a href=\"http://nginx.org/\">nginx.org</a>.<br/>\nCommercial support is available at\n<a href=\"http://nginx.com/\">nginx.com</a>.</p>\n\n<p><em>Thank you for using nginx.</em></p>\n</body>\n</html>\n"}
23:09:48.339 [pipeline-pool-thread-1  ] INFO  -add(1,2) = 3
23:09:48.382 [pipeline-pool-thread-1  ] INFO  -{"code":0,"stdout":"\r\nWindows IP 配置\r\n\r\n\r\n以太网适配器 以太网:\r\n\r\n   连接特定的 DNS 后缀 . . . . . . . : \r\n   IPv4 地址 . . . . . . . . . . . . : 192.168.0.103\r\n   子网掩码  . . . . . . . . . . . . : 255.255.255.0\r\n   默认网关. . . . . . . . . . . . . : 192.168.0.1\r\n\r\n以太网适配器 以太网 6:\r\n\r\n   媒体状态  . . . . . . . . . . . . : 媒体已断开连接\r\n   连接特定的 DNS 后缀 . . . . . . . : \r\n\r\n隧道适配器 Teredo Tunneling Pseudo-Interface:\r\n\r\n   连接特定的 DNS 后缀 . . . . . . . : \r\n   IPv6 地址 . . . . . . . . . . . . : 2001:0:2851:b9f0:28f3:a26a:ceb7:3a90\r\n   本地链接 IPv6 地址. . . . . . . . : fe80::28f3:a26a:ceb7:3a90%12\r\n   默认网关. . . . . . . . . . . . . : ::\r\n"}
23:09:48.382 [collector-pool-thread-1 ] INFO  ---------Pipeline[counters_pipeline] Stage[抓取counter指标] parallel Script[counter_metric_js] end
23:09:48.382 [collector-pool-thread-1 ] INFO  ---------Pipeline[counters_pipeline] Stage[抓取counter指标] parallel Script[counter_metric2_js] end
23:09:48.382 [collector-pool-thread-1 ] INFO  ---------Pipeline[counters_pipeline] Stage[抓取counter指标] end
23:09:48.382 [collector-pool-thread-1 ] INFO  ---------Pipeline[counters_pipeline] end
23:09:48.382 [http-nio-8030-exec-2    ] INFO  -----Collector[counters_collector] end
23:09:48.382 [http-nio-8030-exec-2    ] INFO  -----Collector[gauges_collector] end
23:09:48.382 [http-nio-8030-exec-2    ] INFO  -----Collector[timers_collector] end
23:09:48.382 [http-nio-8030-exec-2    ] INFO  -----Collector[summaries_collector] end
23:09:48.382 [http-nio-8030-exec-2    ] INFO  -Collector collect end
23:09:48.382 [http-nio-8030-exec-2    ] INFO  -Trigger AFTER COLLECT start
23:09:48.382 [http-nio-8030-exec-2    ] INFO  -----Trigger AFTER COLLECT Pipeline[after_pipeline] start
23:09:48.382 [http-nio-8030-exec-2    ] INFO  ---------Pipeline[after_pipeline] start
23:09:48.382 [http-nio-8030-exec-2    ] INFO  ---------Pipeline[after_pipeline] Stage[后置流程] start
23:09:48.382 [http-nio-8030-exec-2    ] INFO  ---------Pipeline[after_pipeline] Stage[后置流程] serial Script[after_js] start
23:09:48.387 [http-nio-8030-exec-2    ] INFO  -after
23:09:48.387 [http-nio-8030-exec-2    ] INFO  ---------Pipeline[after_pipeline] Stage[后置流程] serial Script[after_js] end
23:09:48.387 [http-nio-8030-exec-2    ] INFO  ---------Pipeline[after_pipeline] end
23:09:48.387 [http-nio-8030-exec-2    ] INFO  -----Trigger AFTER COLLECT Pipeline[after_pipeline] end
23:09:48.387 [http-nio-8030-exec-2    ] INFO  -Trigger AFTER COLLECT end
23:09:48.387 [http-nio-8030-exec-2    ] INFO  -Collect end

23:09:48.406 [http-nio-8030-exec-2    ] DEBUG -Exporter Yaml: 
collectors:
- cron: null
  name: counters_collector
  pipeline: counters_pipeline
  script: null
- cron: null
  name: gauges_collector
  pipeline: gauges_pipeline
  script: null
- cron: 0/30 * * * * ?
  name: timers_collector
  pipeline: timers_pipeline
  script: null
- cron: null
  name: summaries_collector
  pipeline: summaries_pipeline
  script: null
meters:
  counters:
  - desc: counter metric
    name: counter.metric
    tags:
    - tag1
    - tag2
    unit: null
  - desc: counter metric2
    name: counter.metric2
    tags:
    - tag1
    - tag2
    unit: second
  gauges:
  - desc: gauge metric
    name: gauge.metric
    tags:
    - tag1
    - tag2
    unit: null
  - desc: gauge metric2
    name: gauge.metric2
    tags:
    - tag1
    - tag2
    unit: null
  summaries:
  - bufferLength: null
    desc: summary metric
    expiry: null
    maximumExpectedValue: 1000.0
    minimumExpectedValue: 1.0
    name: summary.metric
    percentileHistogram: null
    percentilePrecision: null
    percentiles:
    - 0.5
    - 0.95
    - 0.99
    scale: null
    serviceLevelObjectives: null
    tags:
    - tag1
    - tag2
    timeUnit: MILLISECONDS
    unit: null
  - bufferLength: null
    desc: summary histogram metric
    expiry: null
    maximumExpectedValue: 1100.0
    minimumExpectedValue: 500.0
    name: summary.histogram.metric
    percentileHistogram: true
    percentilePrecision: null
    percentiles: null
    scale: null
    serviceLevelObjectives:
    - 500.0
    - 800.0
    - 900.0
    - 950.0
    - 990.0
    - 1000.0
    tags:
    - tag1
    - tag2
    timeUnit: MILLISECONDS
    unit: null
  timers:
  - bufferLength: null
    desc: timer metric
    expiry: null
    maximumExpectedValue: null
    minimumExpectedValue: null
    name: timer.metric
    percentileHistogram: null
    percentilePrecision: null
    percentiles: null
    serviceLevelObjectives: null
    tags:
    - tag1
    - tag2
    timeUnit: SECONDS
    unit: null
  - bufferLength: null
    desc: timer histogram metric
    expiry: null
    maximumExpectedValue: 1000.0
    minimumExpectedValue: 1.0
    name: timer.histogram.metric
    percentileHistogram: true
    percentilePrecision: null
    percentiles:
    - 0.5
    - 0.95
    - 0.99
    serviceLevelObjectives:
    - 500.0
    - 800.0
    - 900.0
    - 950.0
    - 990.0
    - 1000.0
    tags:
    - tag1
    - tag2
    timeUnit: MILLISECONDS
    unit: null
monitors:
- !!pers.laineyc.prometheusexporterframe.enums.Monitor 'PROMETHEUS'
name: demo-exporter
params:
- name: serverUrl
  nest: []
  required: true
  type: STRING
  value: null
- name: username
  nest: []
  required: true
  type: STRING
  value: null
- name: password
  nest: []
  required: true
  type: STRING
  value: null
- name: account
  nest:
  - name: username
    nest: []
    required: true
    type: STRING
    value: null
  - name: password
    nest: []
    required: true
    type: STRING
    value: null
  required: true
  type: NEST
  value: null
pipelines:
- name: before_pipeline
  stages:
  - parallel: []
    serial:
    - before_js
    title: 前置流程
- name: counters_pipeline
  stages:
  - parallel:
    - counter_metric_js
    - counter_metric2_js
    serial: []
    title: 抓取counter指标
- name: gauges_pipeline
  stages:
  - parallel: []
    serial:
    - gauges_js
    title: 抓取gauge指标
- name: timers_pipeline
  stages:
  - parallel: []
    serial:
    - timers_js
    title: 抓取timer指标
- name: summaries_pipeline
  stages:
  - parallel: []
    serial:
    - summaries_js
    title: 抓取summary指标
- name: after_pipeline
  stages:
  - parallel: []
    serial:
    - after_js
    title: 后置流程
scripts:
- file: before.js
  name: before_js
- file: counter_metric.js
  name: counter_metric_js
- file: counter_metric2.js
  name: counter_metric2_js
- file: gauges.js
  name: gauges_js
- file: timers.js
  name: timers_js
- file: summaries.js
  name: summaries_js
- file: after.js
  name: after_js
triggers:
- event: COLLECT
  pipeline: null
  script: before_js
  time: BEFORE
- event: COLLECT
  pipeline: after_pipeline
  script: null
  time: AFTER

23:09:48.412 [http-nio-8030-exec-2    ] DEBUG -Case Yaml: 
collectors:
- counters_collector
- gauges_collector
- timers_collector
- summaries_collector
commonTags:
  env: test
exporter: demo-exporter
logging:
  level: ALL
  maxFileSize: 100MB
  maxHistory: 30
  totalSizeCap: 1GB
monitor: PROMETHEUS
name: demo-exporter-case
params:
  password: password
  serverUrl: http://blog.ccvv.icu
  account:
    password: account.password
    username: account.username
  username: username

23:09:48.421 [http-nio-8030-exec-2    ] DEBUG -Metric Result: 
# HELP counter_metric_total counter metric
# TYPE counter_metric_total counter
counter_metric_total{env="test",tag1="t1",tag2="t2",} 1.0
# HELP timer_metric_seconds_max timer metric
# TYPE timer_metric_seconds_max gauge
timer_metric_seconds_max{env="test",tag1="t1",tag2="t2",} 5.0
# HELP timer_metric_seconds timer metric
# TYPE timer_metric_seconds summary
timer_metric_seconds_count{env="test",tag1="t1",tag2="t2",} 1.0
timer_metric_seconds_sum{env="test",tag1="t1",tag2="t2",} 5.0
# HELP counter_metric2_second_total counter metric2
# TYPE counter_metric2_second_total counter
counter_metric2_second_total{env="test",tag1="t1",tag2="t2",} 2.0
# HELP timer_histogram_metric_seconds timer histogram metric
# TYPE timer_histogram_metric_seconds histogram
timer_histogram_metric_seconds{env="test",tag1="t1",tag2="t2",quantile="0.5",} 0.004980736
timer_histogram_metric_seconds{env="test",tag1="t1",tag2="t2",quantile="0.95",} 0.004980736
timer_histogram_metric_seconds{env="test",tag1="t1",tag2="t2",quantile="0.99",} 0.004980736
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.001",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.001048576",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.001398101",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.001747626",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.002097151",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.002446676",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.002796201",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.003145726",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.003495251",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.003844776",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.004194304",} 0.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.005592405",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.006990506",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.008388607",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.009786708",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.011184809",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.01258291",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.013981011",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.015379112",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.016777216",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.022369621",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.027962026",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.033554431",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.039146836",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.044739241",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.050331646",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.055924051",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.061516456",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.067108864",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.089478485",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.111848106",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.134217727",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.156587348",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.178956969",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.20132659",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.223696211",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.246065832",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.268435456",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.357913941",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.447392426",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.5",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.536870911",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.626349396",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.715827881",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.8",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.805306366",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.894784851",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.9",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.95",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.984263336",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="0.99",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="1.0",} 1.0
timer_histogram_metric_seconds_bucket{env="test",tag1="t1",tag2="t2",le="+Inf",} 1.0
timer_histogram_metric_seconds_count{env="test",tag1="t1",tag2="t2",} 1.0
timer_histogram_metric_seconds_sum{env="test",tag1="t1",tag2="t2",} 0.005
# HELP timer_histogram_metric_seconds_max timer histogram metric
# TYPE timer_histogram_metric_seconds_max gauge
timer_histogram_metric_seconds_max{env="test",tag1="t1",tag2="t2",} 0.005
# HELP summary_metric summary metric
# TYPE summary_metric summary
summary_metric{env="test",tag1="t1",tag2="t2",quantile="0.5",} 5.0
summary_metric{env="test",tag1="t1",tag2="t2",quantile="0.95",} 5.0
summary_metric{env="test",tag1="t1",tag2="t2",quantile="0.99",} 5.0
summary_metric_count{env="test",tag1="t1",tag2="t2",} 1.0
summary_metric_sum{env="test",tag1="t1",tag2="t2",} 5.0
# HELP summary_metric_max summary metric
# TYPE summary_metric_max gauge
summary_metric_max{env="test",tag1="t1",tag2="t2",} 5.0
# HELP gauge_metric gauge metric
# TYPE gauge_metric gauge
gauge_metric{env="test",tag1="t1",tag2="t2",} 3.0
# HELP gauge_metric2 gauge metric2
# TYPE gauge_metric2 gauge
gauge_metric2{env="test",tag1="t1",tag2="t2",} 4.0
# HELP summary_histogram_metric summary histogram metric
# TYPE summary_histogram_metric histogram
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="500.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="511.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="596.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="681.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="766.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="800.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="851.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="900.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="936.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="950.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="990.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="1000.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="1024.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="1100.0",} 1.0
summary_histogram_metric_bucket{env="test",tag1="t1",tag2="t2",le="+Inf",} 1.0
summary_histogram_metric_count{env="test",tag1="t1",tag2="t2",} 1.0
summary_histogram_metric_sum{env="test",tag1="t1",tag2="t2",} 5.0
# HELP summary_histogram_metric_max summary histogram metric
# TYPE summary_histogram_metric_max gauge
summary_histogram_metric_max{env="test",tag1="t1",tag2="t2",} 5.0


```

## 3.根据调试提炼Case

**case.yml**
```yaml
name: demo-exporter-case
exporter: demo-exporter
monitor: PROMETHEUS
params:
  serverUrl: http://blog.ccvv.icu
  username: username
  password: password
  account:
    username: account.username
    password: account.password
commonTags:
  env: test
collectors:
  - counters_collector
  - gauges_collector
  - timers_collector
  - summaries_collector
```
