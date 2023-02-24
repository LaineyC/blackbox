package pers.laineyc.blackbox.api;

import lombok.Data;

@Data
public class ResponseMessage<T> {

    protected String code;

    protected String message;

    protected T result;

    protected Long timestamp;

    public ResponseMessage() {
    }

    public static <T> ResponseMessage<T> error(String message) {
        return error("500",  message);
    }

    public static <T> ResponseMessage<T> error(String code, String message, Object... args) {
        ResponseMessage<T> msg = new ResponseMessage<>();
        msg.message = String.format(message, args);
        msg.code = code;
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }

    public static <T> ResponseMessage<T> ok() {
        return ok((T)null);
    }

    public static <T> ResponseMessage<T> ok(T result) {
        ResponseMessage<T> msg = new ResponseMessage<>();
        msg.result = result;
        msg.code = "0";
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }

}
