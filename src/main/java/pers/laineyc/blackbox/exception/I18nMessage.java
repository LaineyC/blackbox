package pers.laineyc.blackbox.exception;

public enum I18nMessage {

    PARAM_CHECK("param.check.error", "参数验证错误");

    private String code;

    private String desc;

    I18nMessage(String code, String desc) {
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
