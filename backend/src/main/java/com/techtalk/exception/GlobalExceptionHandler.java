package com.techtalk.exception;

import com.techtalk.common.BusinessException;
import com.techtalk.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验异常 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(Exception ex) {
        String message;
        if (ex instanceof MethodArgumentNotValidException validEx) {
            message = validEx.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        } else if (ex instanceof BindException bindEx) {
            message = bindEx.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        } else {
            message = "参数校验失败";
        }
        return Result.badRequest(message);
    }

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: [{}] {}", ex.getCode(), ex.getMessage());
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException ex) {
        return Result.forbidden("权限不足");
    }

    /** 其他异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.fail("服务器内部错误，请稍后重试");
    }
}
