package com.techtalk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateDTO {

    @Size(min = 0, max = 200, message = "签名长度不超过 200 位")
    private String bio;

    private String avatar;
}
