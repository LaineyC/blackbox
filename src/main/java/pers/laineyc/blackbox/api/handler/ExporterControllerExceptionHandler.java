package pers.laineyc.blackbox.api.handler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pers.laineyc.blackbox.api.ExporterController;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.exception.BusinessException;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.exception.I18nMessage;
import pers.laineyc.blackbox.service.I18nService;
import pers.laineyc.blackbox.service.LoggerService;
import pers.laineyc.blackbox.util.ThreadLocalUtil;

@Slf4j
@ControllerAdvice(assignableTypes = ExporterController.class)
public class ExporterControllerExceptionHandler {

    @Autowired
    private I18nService i18nService;

    @Autowired
    private LoggerService loggerService;

    @ExceptionHandler({Throwable.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void handleException(Throwable exception, HttpServletResponse httpServletResponse) throws Exception{
        log.error("Handle UnknownException. ", exception);
        String message = exception.getMessage();
        Logger logger = ThreadLocalUtil.get(Constant.LOGGER);
        logger.error(message);
        String info = loggerService.getByteArrayOutputStreamLoggerInfo(logger);
        httpServletResponse.addHeader("Content-Type", "text/plain;charset=UTF-8");
        httpServletResponse.getWriter().print(info);
    }

    @ExceptionHandler({CommonException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void handleException(CommonException exception, HttpServletResponse httpServletResponse) throws Exception{
        String message = exception.getMessage();
        Logger logger = ThreadLocalUtil.get(Constant.LOGGER);
        logger.error(message);
        String info = loggerService.getByteArrayOutputStreamLoggerInfo(logger);
        httpServletResponse.addHeader("Content-Type", "text/plain;charset=UTF-8");
        httpServletResponse.getWriter().print(info);
    }

    @ExceptionHandler({BusinessException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void handleException(BusinessException exception, HttpServletResponse httpServletResponse) throws Exception{
        I18nMessage i18nMessage = exception.getI18nMessage();
        String message = i18nService.getMessage(i18nMessage.getCode(), exception.getArgs());
        Logger logger = ThreadLocalUtil.get(Constant.LOGGER);
        logger.error(message);
        String info = loggerService.getByteArrayOutputStreamLoggerInfo(logger);
        httpServletResponse.addHeader("Content-Type", "text/plain;charset=UTF-8");
        httpServletResponse.getWriter().print(info);
    }

}