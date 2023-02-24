package pers.laineyc.blackbox.exception;

public enum ErrorCode {

    SUCCESS("200", ""),

    CLIENT_ERROR("400", ""),

    SERVER_ERROR("500", "");

    private String code;

    private String desc;

    ErrorCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
