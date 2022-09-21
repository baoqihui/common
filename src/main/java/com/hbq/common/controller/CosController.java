package com.hbq.common.controller;

import com.hbq.common.util.CosUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 测试Cos文件系统
 */
@Slf4j
@CrossOrigin
@Api(tags = "文件")
@RestController
@RequestMapping("/cos")
public class CosController {
    @Value("${cos.bucketName}")
    private String bucketName;

    @ApiOperation(value = "上传文件返回url")
    @PostMapping("/upload")
    public String CosUpload(MultipartFile file) throws Exception {
        return CosUtil.uploadPreview(bucketName, file);
    }
}

