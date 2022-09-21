package com.hbq.common.model;

/**
 * @Author: huibq
 * @Date: 2022/5/7 15:47
 * @Description: 验证码类型
 */
public enum CodeType {
    /**
     * 密码验证
     */
    PWD(1, "密码验证"),
    /**
     * 手机验证
     */
    TEL(2, "手机验证");

    private Integer code;
    private String desc;

    CodeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
