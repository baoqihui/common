package com.hbq.common.controller;

import com.hbq.common.model.MessageDTO;
import com.hbq.common.model.Result;
import com.hbq.common.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author: huibq
 * @Date: 2022/5/6 16:11
 * @Description: 公共方法
 */
@CrossOrigin
@RestController
@Api(tags = "短信")
@AllArgsConstructor
@RequestMapping("/msg")
public class MsgController {
    private MessageUtil messageUtil;

    @ApiOperation(value = "发送短信")
    @PostMapping(value="/send")
    public Result sendMessage(@Valid @RequestBody MessageDTO messageDTO){
        messageUtil.sendMessage(messageDTO.getTel());
        return Result.succeed("发送成功");
    }

    @ApiOperation(value = "验证短信验证码")
    @PostMapping(value="/check")
    public Result checkMessage(@Valid @RequestBody MessageDTO messageDTO){
        if(messageUtil.isCode(messageDTO.getTel(),messageDTO.getCode())){
            return Result.succeed("短信验证成功");
        }else{
            return Result.failed("短信验证失败");
        }
    }
}
