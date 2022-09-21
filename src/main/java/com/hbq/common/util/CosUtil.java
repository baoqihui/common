package com.hbq.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;


/**
 * 文件服务器工具类
 */
@Slf4j
@Component
public class CosUtil {
    private static COSClient cosClient;
    @Value("${cos.accessKey}")
    private String accessKey;
    @Value("${cos.secretKey}")
    private String secretKey;
    @Value("${cos.regionName}")
    private String regionName;

    @PostConstruct
    public void init() {
        try {
            COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
            Region region = new Region(regionName);
            ClientConfig clientConfig = new ClientConfig(region);
            cosClient = new COSClient(cred, clientConfig);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("初始化cos配置异常:", e);
        }
    }

    /**
     * 上传预览
     *
     * @param file 文件
     * @return 文件url
     * @throws IOException
     */
    public static String uploadPreview(String bucketName, MultipartFile file) throws IOException {
        String newFileName = upload(bucketName, file);
        return preview(bucketName, newFileName);
    }

    /**
     * 预览
     *
     * @param bucketName bucket名称
     * @param fileName   文件名称
     * @return 预览url
     */
    private static String preview(String bucketName, String fileName) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, fileName);
        COSObject cosObject = cosClient.getObject(getObjectRequest);
        return cosObject.getObjectContent().getHttpRequest().getURI().toString();
    }

    /**
     * 上传文件
     *
     * @param bucketName bucket名称
     * @param file       文件
     * @return 文件url
     * @throws IOException
     */
    private static String upload(String bucketName, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newFileName = String.format("%s-%s.%s", FileUtil.mainName(originalFilename), IdUtil.simpleUUID(), FileUtil.extName(originalFilename));
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, newFileName, file.getInputStream(), new ObjectMetadata());
        cosClient.putObject(putObjectRequest);
        return newFileName;
    }
}