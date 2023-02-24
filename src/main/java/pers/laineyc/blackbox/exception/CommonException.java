package pers.laineyc.blackbox.exception;

public class CommonException extends RuntimeException {

    private String code;

    public CommonException(String message, Object... args) {
        this(ErrorCode.SERVER_ERROR.getCode(), message, args);
    }

    public CommonException(String code, String message, Object... args) {
        super(String.format(message, args), null, false, false);
        this.code = code;
    }

    public CommonException(Throwable cause, String message, Object... args) {
        super(String.format(message, args), cause);
        this.code = ErrorCode.SERVER_ERROR.getCode();
    }

    public CommonException(Throwable cause, String code, String message, Object... args) {
        super(String.format(message, args), cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
