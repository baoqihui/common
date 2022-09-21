package com.hbq.common.config;

import com.hbq.common.model.ErrorEnum;
import com.hbq.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;

/**
 * @author: hbq
 * @description: 统一异常拦截
 * @date: 2017/10/24 10:31
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("校验失败：{}\nat：{}", e.getMessage(), Arrays.toString(e.getStackTrace()).replaceAll(",", "\n   "));
        String message = e.getFieldError().getDefaultMessage();
        String errorDetail = message + "(" + e.getFieldError().getField() + ")";
        return Result.failed(errorDetail);
    }

    /**
     * 参数JSON解析异常处理
     *
     * @param e 参数解析异常
     * @return 返回结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleJsonException(Exception e) {
        log.error("JSON解析异常：", e);
        return Result.failedWith(e.getMessage(), ErrorEnum.E_205.getErrorCode(), ErrorEnum.E_205.getErrorMsg());
    }

    /**
     * 常规异常处理
     */
    @ExceptionHandler(Throwable.class)
    public Result handleGeneralException(Exception e) {
        log.error("未知异常：", e);
        return Result.failedWith("", ErrorEnum.E_500.getErrorCode(), ErrorEnum.E_500.getErrorMsg());
    }
}
