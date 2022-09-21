package com.hbq.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hbq.common.model.FileVo;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件服务器工具类
 */
@Slf4j
@Component
public class MinioUtil {
    private static MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.bucketName}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            createBucket(bucketName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("初始化minio配置异常: 【{}】", e.fillInStackTrace());
        }
    }


    /**
     * 查看存储bucket是否存在
     *
     * @return boolean
     */
    public static Boolean bucketExists(String bucketName) {
        Boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return found;
    }

    /**
     * 创建存储bucket
     *
     * @return Boolean
     */
    public static Boolean createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     *
     * @return Boolean
     */
    public static Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取全部bucket
     */
    public static List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件上传
     *
     * @param file 文件
     * @return Boolean
     */
    public static Boolean upload(String bucketName, String fileName, MultipartFile file, InputStream inputStream) {
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(FileUtil.getMimeType(fileName))
                    .build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 上传预览
     *
     * @param bucketName 位置
     * @param file       文件
     * @return 文件url
     * @throws IOException
     */
    public static String uploadPreview(String bucketName, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newFileName = String.format("%s-%s.%s", FileUtil.mainName(originalFilename), IdUtil.simpleUUID(), FileUtil.extName(originalFilename));
        upload(bucketName, newFileName, file, file.getInputStream());
        return preview(bucketName, newFileName);
    }

    /**
     * 预览
     *
     * @param fileName
     * @return
     */
    public static String preview(String bucketName, String fileName) {
        // 查看文件地址`
        try {
            GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .method(Method.GET)
                    .build();
            String url = minioClient.getPresignedObjectUrl(build);
            String finalUrl = StrUtil.subBefore(url, "?", false);
            log.info("文件路径预览：{}", finalUrl);
            return finalUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件下载
     *
     * @param fileName 文件名称
     * @param res      response
     * @return Boolean
     */
    public static void download(String fileName, String bucketName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName)
                .object(fileName).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs)) {
            byte[] buf = new byte[1024];
            int len;
            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()) {
                while ((len = response.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
                os.flush();
                byte[] bytes = os.toByteArray();
                res.setCharacterEncoding("utf-8");
                //设置强制下载不打开
                //res.setContentType("application/force-download");
                res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                try (ServletOutputStream stream = res.getOutputStream()) {
                    stream.write(bytes);
                    stream.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查看文件对象
     *
     * @return 存储bucket内文件对象信息
     */
    public static List<Item> listObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(true).build());
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }

    /**
     * 所有文件列表
     *
     * @param bucketName
     * @return
     */
    public static List<FileVo> listObjectNames(String bucketName) {
        List<Item> items = listObjects(bucketName);
        if (ObjectUtil.isEmpty(items)) {
            return new ArrayList<>();
        } else {
            return items.stream()
                    .map(i -> FileVo.builder()
                            .fileName(i.objectName())
                            .fileSize(i.size())
                            .filePath(preview(bucketName, i.objectName()))
                            .fileType(FileUtil.extName(i.objectName()))
                            .build()).collect(Collectors.toList());

        }
    }
    /**
     * 所有文件url
     * @param bucketName
     * @return
     */
    public static List<String> listObjectUrls(String bucketName) {
        List<Item> items = listObjects(bucketName);
        if (ObjectUtil.isEmpty(items)) {
            return new ArrayList<>();
        } else {
            return items.stream()
                    .map(i-> preview(bucketName,i.objectName()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 随机获取一个文件
     */
    public static String random(String bucketName) {
        List<String> urls = listObjectUrls(bucketName);
        return urls.get(RandomUtil.randomInt(urls.size()));
    }
    /**
     * 删除
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static boolean remove(String fileName, String bucketName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 批量删除文件对象（没测试）
     *
     * @param objects 对象名称集合
     */
    public static Iterable<Result<DeleteError>> removeObjects(List<String> objects, String bucketName) {
        List<DeleteObject> dos = objects.stream().map(e -> new DeleteObject(e)).collect(Collectors.toList());
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
        return results;
    }

}