package pers.laineyc.blackbox.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import pers.laineyc.blackbox.support.AccessLogInterceptor;
import pers.laineyc.blackbox.util.ThreadLocalUtil;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfigurer extends WebMvcConfigurationSupport {

    @Bean
    public HandlerInterceptor threadLocalClearInterceptor() {
        return new HandlerInterceptor() {
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
                ThreadLocalUtil.clear();
            }
        };
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AccessLogInterceptor());
        registry.addInterceptor(this.threadLocalClearInterceptor());
    }

    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(fastJsonHttpMessageConverter());
        converters.add(stringUtf8HttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    private FastJsonHttpMessageConverter4 fastJsonHttpMessageConverter(){
        //1、定义一个convert转换消息的对象
        FastJsonHttpMessageConverter4 fastConverter = new FastJsonHttpMessageConverter4();
        //2、添加FastJson的配置信息
        FastJsonConfig fastJsonConfig = new FastJsonConfig();

        //Long类型转String类型
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        // serializeConfig.put(Long.TYPE, ToStringSerializer.instance); //不转long值
        fastJsonConfig.setSerializeConfig(serializeConfig);

        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.WriteMapNullValue, // 保留map空的字段
                //SerializerFeature.WriteNullStringAsEmpty, // 将String类型的null转成""
                //SerializerFeature.WriteNullNumberAsZero, // 将Number类型的null转成0
                SerializerFeature.WriteNullListAsEmpty, // 将List类型的null转成[]
                //SerializerFeature.WriteNullBooleanAsFalse, // 将Boolean类型的null转成false
                SerializerFeature.WriteDateUseDateFormat  //日期格式转换
                //SerializerFeature.DisableCircularReferenceDetect // 避免循环引用
        );
        //3、在convert中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //4、解决响应数据非json和中文响应乱码
        List<MediaType> jsonMediaTypes = new ArrayList<>();
        jsonMediaTypes.add(MediaType.APPLICATION_JSON);
        fastConverter.setSupportedMediaTypes(jsonMediaTypes);

        return fastConverter;
    }

    @Bean
    public HttpMessageConverter<String> stringUtf8HttpMessageConverter(){
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converter.setWriteAcceptCharset(false);
        return converter;
    }

}

