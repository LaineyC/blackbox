package pers.laineyc.blackbox.support;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.UUID;

@Slf4j
public class AccessLogInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String uuid = UUID.randomUUID().toString();
        log.info(String.format("Request[%s] URL:[%s], Protocol:[%s], Params:%s", uuid, request.getRequestURL(), request.getProtocol(), JSONObject.toJSONString(request.getParameterMap())));
        request.setAttribute("startTime", System.currentTimeMillis());
        request.setAttribute("uuid", uuid);
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) throws Exception {
        long timeout = System.currentTimeMillis() - (Long)request.getAttribute("startTime");
        String uuid = (String)request.getAttribute("uuid");
        log.info(String.format("Response[%s][%s] Timeout:[%s ms], ResponseStatus:[%s], ResponseBodySize:[%s], Error:[%s]", uuid, request.getRequestURI(), timeout, response.getStatus(), response.getBufferSize(), e != null ? e.getMessage() : "null"));
    }

}
