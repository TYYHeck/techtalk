package com.techtalk.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    // ---- 成功 ----
    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null, System.currentTimeMillis());
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data, System.currentTimeMillis());
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }

    // ---- 失败 ----
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    // ---- 常用错误码 ----
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null, System.currentTimeMillis());
    }
}
