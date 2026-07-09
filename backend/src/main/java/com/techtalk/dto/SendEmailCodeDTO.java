package com.techtalk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送邮箱验证码请求
 */
@Data
public class SendEmailCodeDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 用途：register-注册，resetPassword-重置密码 */
    @NotBlank(message = "用途不能为空")
    private String purpose;
}
