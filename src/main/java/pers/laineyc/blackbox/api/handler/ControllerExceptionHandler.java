package pers.laineyc.blackbox.api.handler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pers.laineyc.blackbox.api.ResponseMessage;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.exception.BusinessException;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.exception.ErrorCode;
import pers.laineyc.blackbox.exception.I18nMessage;
import pers.laineyc.blackbox.service.I18nService;
import pers.laineyc.blackbox.service.LoggerService;
import pers.laineyc.blackbox.util.ThreadLocalUtil;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @Autowired
    private I18nService i18nService;

    @Autowired
    private LoggerService loggerService;

    @ExceptionHandler({Throwable.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(Throwable exception) {
        log.error("Handle UnknownException. ", exception);

        String code = ErrorCode.SERVER_ERROR.getCode();
        String message = exception.getMessage();

        Logger logger = ThreadLocalUtil.get(Constant.LOGGER);
        if(logger != null) {
            logger.error(message);
            return loggerService.getByteArrayOutputStreamLoggerInfo(logger);
        }
        else{
            return ResponseMessage.error(code, message);
        }
    }

    @ExceptionHandler({CommonException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Object handleException(CommonException exception) {
        String code = exception.getCause() != null ? ErrorCode.SERVER_ERROR.getCode() : ErrorCode.CLIENT_ERROR.getCode();
        String message = exception.getMessage();

        Logger logger = ThreadLocalUtil.get(Constant.LOGGER);
        if(logger != null) {
            logger.error(message);
            return loggerService.getByteArrayOutputStreamLoggerInfo(logger);
        }
        else{
            return ResponseMessage.error(code, message);
        }
    }

    @ExceptionHandler({BusinessException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Object handleException(BusinessException exception) {
        I18nMessage i18nMessage = exception.getI18nMessage();
        String message = i18nService.getMessage(i18nMessage.getCode(), exception.getArgs());
        String code = exception.getCause() != null ? ErrorCode.SERVER_ERROR.getCode() : ErrorCode.CLIENT_ERROR.getCode();

        Logger logger = ThreadLocalUtil.get(Constant.LOGGER);
        if(logger != null) {
            logger.error(message);
            return loggerService.getByteArrayOutputStreamLoggerInfo(logger);
        }
        else{
            return ResponseMessage.error(code, message);
        }
    }

}