package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 项目自定义异常
    @ResponseBody
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 该异常枚举错误码为500，
    public RestErrorResponse customException(XueChengPlusException exception) {
        log.error("系统异常：{}", exception.getErrMessage(),exception);
        return new RestErrorResponse(exception.getErrMessage());
    }

    // 系统异常
    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception exception) {
        log.error("系统异常：{}", exception.getMessage());
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    // JSR303校验抛出的异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        // 由于用户输入的内容可能存在多处错误，所以我们要将所有错误信息都提示给用户
        BindingResult bindingResult = exception.getBindingResult();
        // 获取错误集合
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        // 拼接字符串
        StringBuffer stringBuffer = new StringBuffer();
        fieldErrors.forEach(fieldError -> stringBuffer.append(fieldError.getDefaultMessage()).append(","));
        // 记录日志
        log.error(stringBuffer.toString());
        // 响应给用户
        return new RestErrorResponse(stringBuffer.toString());
    }
}