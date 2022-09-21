package com.hbq.common.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hbq.common.model.FileVo;
import com.hbq.common.model.HexoToken;
import com.hbq.common.util.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: huibq
 * @Date: 2022/5/7 11:41
 * @Description: 文件
 */
@Slf4j
@CrossOrigin
@Api(tags = "文件")
@RestController
@RequestMapping("/file")
@AllArgsConstructor
public class MinioController {

    @ApiOperation(value = "转发token")
    @PostMapping("/get_access_token")
    public JSONObject getVerifyCode(@RequestBody HexoToken hexoToken) {
        String url = "https://github.com/login/oauth/access_token";
        String post = HttpRequest.post(url)
                .header("accept", "application/json")
                .body(JSONUtil.toJsonStr(hexoToken), "application/json")
                .execute().body();
        log.info(post);
        return JSONUtil.parseObj(post);
    }

    @ApiOperation(value = "创建文件夹")
    @PostMapping("/createBucket")
    public void createBucket(String bucketName) {
        MinioUtil.createBucket(bucketName);
    }

    @ApiOperation(value = "上传文件返回url")
    @PostMapping("/upload")
    public String MinIOUpload(String bucketName, MultipartFile file) throws Exception {
        if (ObjectUtil.isEmpty(bucketName)) {
            bucketName = "file";
        }
        return MinioUtil.uploadPreview(bucketName, file);
    }

    @ApiOperation(value = "下载文件")
    @PostMapping("/download")
    public void download(String bucketName, String fileName, HttpServletResponse response) {
        MinioUtil.download(bucketName, fileName, response);
    }

    @ApiOperation(value = "删除文件")
    @PostMapping("/deleteFile")
    public void deleteFile(String bucketName, String fileName) {
        MinioUtil.remove(bucketName, fileName);
    }

    @ApiOperation(value = "获取当前文件夹下文件")
    @PostMapping("/list")
    public List<FileVo> list(String bucketName) {
        return MinioUtil.listObjectNames(bucketName);
    }

    @ApiOperation(value = "获取当前文件夹下url")
    @GetMapping("/urls/{bucketName}")
    public List<String> urls(@PathVariable String bucketName) {
        return MinioUtil.listObjectUrls(bucketName);
    }

    @ApiOperation(value = "随机获取一个url")
    @GetMapping("/random/{bucketName}")
    public String random(@PathVariable String bucketName) {
        return MinioUtil.random(bucketName);
    }
}

