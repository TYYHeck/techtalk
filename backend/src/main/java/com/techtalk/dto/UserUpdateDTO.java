package com.techtalk.dto;

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

    @Size(min = 0, max = 50, message = "昵称长度不超过 50 位")
    private String nickname;

    @Size(min = 0, max = 100, message = "位置长度不超过 100 位")
    private String location;

    @Size(min = 0, max = 100, message = "网站长度不超过 100 位")
    private String website;

    @Size(min = 0, max = 100, message = "GitHub 长度不超过 100 位")
    private String github;
}
