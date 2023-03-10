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
        //1???????????????convert?????????????????????
        FastJsonHttpMessageConverter4 fastConverter = new FastJsonHttpMessageConverter4();
        //2?????????FastJson???????????????
        FastJsonConfig fastJsonConfig = new FastJsonConfig();

        //Long?????????String??????
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        // serializeConfig.put(Long.TYPE, ToStringSerializer.instance); //??????long???
        fastJsonConfig.setSerializeConfig(serializeConfig);

        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.WriteMapNullValue, // ??????map????????????
                //SerializerFeature.WriteNullStringAsEmpty, // ???String?????????null??????""
                //SerializerFeature.WriteNullNumberAsZero, // ???Number?????????null??????0
                SerializerFeature.WriteNullListAsEmpty, // ???List?????????null??????[]
                //SerializerFeature.WriteNullBooleanAsFalse, // ???Boolean?????????null??????false
                SerializerFeature.WriteDateUseDateFormat  //??????????????????
                //SerializerFeature.DisableCircularReferenceDetect // ??????????????????
        );
        //3??????convert?????????????????????
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //4????????????????????????json?????????????????????
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

