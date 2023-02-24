package pers.laineyc.blackbox.config;

import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import pers.laineyc.blackbox.support.I18nLocaleResolver;
import pers.laineyc.blackbox.support.ParamValidInterceptor;

@Configuration
public class Configurer {

    @Bean
    public LocaleResolver i18nLocaleResolver(){
        return new I18nLocaleResolver();
    }

    @Bean
    public ParamValidInterceptor paramValidInterceptor(){
        return new ParamValidInterceptor();
    }

    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        ResourceBundleMessageSource rbms = new ResourceBundleMessageSource();
        rbms.setDefaultEncoding("UTF-8");
        rbms.setBasenames("messages");
        return rbms;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(ResourceBundleMessageSource resourceBundleMessageSource) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();
        factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
        factoryBean.getValidationPropertyMap().put("hibernate.validator.fail_fast", "true");
        factoryBean.setValidationMessageSource(resourceBundleMessageSource);
        return factoryBean;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        // 创建任务调度线程池
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        // 初始化线程池数量
        taskScheduler.setPoolSize(4);
        // 是否将取消后的任务，从队列中删除
        taskScheduler.setRemoveOnCancelPolicy(true);
        // 设置线程名前缀
        taskScheduler.setThreadNamePrefix("scheduled-pool-thread-");
        return taskScheduler;
    }

}
