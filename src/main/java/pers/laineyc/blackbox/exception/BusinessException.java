package pers.laineyc.blackbox.exception;

public class BusinessException extends CommonException {

    private I18nMessage i18nMessage;

    private Object[] args;

    public BusinessException(I18nMessage i18nMessage, Object... args) {
        super(i18nMessage.getCode(), args);
        this.i18nMessage = i18nMessage;
        this.args = args;
    }

    public BusinessException(String code, I18nMessage i18nMessage, Object... args) {
        super(code, i18nMessage.getCode(), args);
        this.i18nMessage = i18nMessage;
        this.args = args;
    }

    public BusinessException(Throwable cause, I18nMessage i18nMessage, Object... args) {
        super(cause, i18nMessage.getCode(), cause);
        this.i18nMessage = i18nMessage;
        this.args = args;
    }

    public BusinessException(Throwable cause, String code, I18nMessage i18nMessage, Object... args) {
        super(cause, code, i18nMessage.getCode(), cause);
        this.i18nMessage = i18nMessage;
        this.args = args;
    }

    public I18nMessage getI18nMessage() {
        return i18nMessage;
    }

    public Object[] getArgs() {
        return args;
    }
}
