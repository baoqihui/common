package com.hbq.common.util;

import cn.hutool.core.util.RandomUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.hbq.common.model.BaseRedisKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @Author: huibq
 * @Date: 2022/5/6 15:33
 * @Description: 短信发送工具类
 */
@Slf4j
@Component
public class MessageUtil {
    private static Client messageClient;

    @Resource
    private RedisUtils redisUtils;

    @Value("${ali.endpoint}")
    private String endpoint;
    @Value("${ali.accessKey}")
    private String accessKey;
    @Value("${ali.secretKey}")
    private String secretKey;
    @Value("${ali.signName}")
    private String signName;
    @Value("${ali.templateCode}")
    private String templateCode;
    @Value("${ali.expireTime}")
    private Long expireTime;

    @PostConstruct
    public void init() {
        try {
            Config config = new Config()
                    // 您的AccessKey ID
                    .setAccessKeyId(accessKey)
                    // 您的AccessKey Secret
                    .setAccessKeySecret(secretKey);
            // 访问的域名
            config.endpoint = endpoint;
            messageClient = new Client(config);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("初始化messageClient配置异常: 【{}】", e.fillInStackTrace());
        }
    }

    /**
     * 发送短信
     *
     * @param tel 手机号
     */
    public void sendMessage(String tel) {
        String code = RandomUtil.randomNumbers(6);
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumbers(tel)
                .setTemplateParam("{\"code\":\"" + code + "\"}");
        try {
            SendSmsResponse sendSmsResponse = messageClient.sendSms(sendSmsRequest);
            String result = sendSmsResponse.getBody().getCode();
            if ("OK".equals(result)) {
                redisUtils.set(String.format(BaseRedisKey.MESSAGE_KEY,tel), code, expireTime);
            }
        } catch (Exception e) {
            log.error("发送短信异常:{}", e.fillInStackTrace());
        }
    }

    /**
     * 验证短信验证码
     * @param tel 手机号
     * @param code 验证码
     * @return 验证结果
     */
    public boolean isCode(String tel, String code) {
        String resultCode = redisUtils.get(String.format(BaseRedisKey.MESSAGE_KEY,tel));
        if (resultCode != null && resultCode.equals(code)) {
            return true;
        }
        return false;
    }

    /**
     * 不是正确的验证码
     * @param tel 手机号
     * @param code 验证码
     * @return 验证结果
     */
    public boolean isNotCode(String tel, String code) {
        return !isCode(tel, code);
    }
}