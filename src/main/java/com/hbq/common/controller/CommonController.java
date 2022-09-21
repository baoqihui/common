package com.hbq.common.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hbq.common.model.HexoToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: huibq
 * @Date: 2022/5/7 11:41
 * @Description: 公共方法
 */
@Slf4j
@CrossOrigin
@Api(tags = "公共方法")
@RestController
@RequestMapping("/common")
@AllArgsConstructor
public class CommonController {
    @ApiOperation(value = "转发token")
    @PostMapping("/access_token")
    public JSONObject getVerifyCode(@RequestBody HexoToken hexoToken) {
        String url = "https://github.com/login/oauth/access_token";
        String post = HttpRequest.post(url)
                .header("accept", "application/json")
                .body(JSONUtil.toJsonStr(hexoToken), "application/json")
                .execute().body();
        log.info(post);
        return JSONUtil.parseObj(post);
    }
}

