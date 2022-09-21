package com.hbq.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @Author: huibq
 * @Date: 2022/5/6 16:50
 * @Description: TelDTO
 */
@Data
public class MessageDTO {

    @ApiModelProperty(value = "手机号")
    @NotBlank(message = "手机号码不能为空")
    @NotNull(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号只能为11位")
    @Pattern(regexp = "^[1][3,4,5,6,7,8,9][0-9]{9}$", message = "手机号格式有误")
    private String tel;

    @ApiModelProperty(value = "验证码")
    private String code;
}
