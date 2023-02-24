package pers.laineyc.blackbox.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout;
import org.springframework.stereotype.Component;
import pers.laineyc.blackbox.model.Logging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

@Slf4j
@Component
public class LoggerService {

    public void stop(org.slf4j.Logger logger){
        if(logger != null) {
            Logger logback = (Logger)logger;
            logback.getAppender("appender").stop();
            logback.getLoggerContext().stop();
        }
    }

    public String getByteArrayOutputStreamLoggerInfo(org.slf4j.Logger logger){
        Logger logback = (Logger)logger;
        OutputStreamAppender<ILoggingEvent> append = (OutputStreamAppender<ILoggingEvent>)logback.getAppender("appender");
        return append.getOutputStream().toString();
    }

    public org.slf4j.Logger newByteArrayOutputStreamLogger(String name, Logging logging){
        return newOutputStreamLogger(name, new ByteArrayOutputStream(), logging);
    }

    private org.slf4j.Logger newOutputStreamLogger(String name, OutputStream outputStream, Logging logging){
        if(logging == null) {
            logging = new Logging();
        }

        LoggerContext lc = new LoggerContext();

        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setContext(lc);
        appender.setName("appender");
        appender.setOutputStream(outputStream);

        setLayoutEncoder(lc, appender, true);

        appender.start();

        Logger log = lc.getLogger(name);
        log.setLevel(Level.toLevel(logging.getLevel().name()));
        log.setAdditive(false);
        log.addAppender(appender);

        return log;
    }

    public org.slf4j.Logger newRollingFileLogger(String name, File logsDir, Logging logging){
        if(logging == null) {
            logging = new Logging();
        }

        //LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        LoggerContext lc = new LoggerContext();

        Logger log = lc.getLogger(name);
        log.setLevel(Level.toLevel(logging.getLevel().name()));
        log.setAdditive(false);

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setName("appender");
        appender.setContext(lc);

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(lc);
        policy.setParent(appender);
        policy.setFileNamePattern(logsDir.getAbsolutePath() + File.separator + "%d{yyyy-MM-dd}.%i.log");
        policy.setMaxFileSize(FileSize.valueOf(logging.getMaxFileSize()));
        policy.setTotalSizeCap(FileSize.valueOf(logging.getTotalSizeCap()));
        policy.setMaxHistory(logging.getMaxHistory());
        policy.start();

        appender.setRollingPolicy(policy);

        setLayoutEncoder(lc, appender, false);

        appender.start();

        log.addAppender(appender);

        return log;
    }

    private void setLayoutEncoder(LoggerContext lc, OutputStreamAppender<ILoggingEvent> appender, boolean debug){
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(lc);
        encoder.start();

        appender.setEncoder(encoder);

        TraceIdPatternLogbackLayout layout = new TraceIdPatternLogbackLayout();
        layout.setContext(lc);
//        layout.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%tid] [%thread] %-5level %logger{36} -%msg%n");
        layout.setPattern(debug ? "%d{HH:mm:ss.SSS} [%-24thread] %-5level -%msg%n" : "%d{yyyy-MM-dd HH:mm:ss.SSS} [%-24thread] %-5level -%msg%n");
        layout.start();

        encoder.setLayout(layout);
    }

}
