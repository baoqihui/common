package com.hbq.common.aop;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;


/**
 * @Author: huibq
 * @Date: 26/3/2022 下午2:42
 * @Description: 日志切面
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    private static final ThreadLocal<StringBuilder> SB = new ThreadLocal<>();

    /**
     * 日志切面
     */
    @Pointcut("execution(public * com.hbq..*Controller.*(..))")
    public void log() {
    }

    /**
     * 进入前打印日志
     *
     * @param joinPoint 切点
     */
    @Before("log()")
    public void doBefore(JoinPoint joinPoint) {
        StringBuilder builder = new StringBuilder();
        START_TIME.set(System.currentTimeMillis());
        SB.set(builder);
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        builder.append(LINE_SEPARATOR)
                .append("--------------doBefore------begin new------------------------")
                .append(LINE_SEPARATOR);
        builder.append("request url : ").append(request.getRequestURL().toString()).append(LINE_SEPARATOR);
        builder.append("request method: ").append(request.getMethod()).append(LINE_SEPARATOR);
        builder.append("request contentType : ").append(request.getContentType()).append(LINE_SEPARATOR);
        builder.append("controller method : ").append(joinPoint.getSignature().getDeclaringTypeName())
                .append(".").append(joinPoint.getSignature().getName()).append(LINE_SEPARATOR);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] objects = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            if (p.getType().equals(HttpServletRequest.class)) {
                continue;
            }
            if (p.getType().equals(HttpServletResponse.class)) {
                continue;
            }
            if (p.getType().equals(Principal.class)) {
                continue;
            }
            if ("bytes".equalsIgnoreCase(p.getName())) {
                continue;
            }
            if (objects[i] != null) {
                Gson gson = new Gson();
                builder.append("request args : ").append(gson.toJson(ImmutableMap.of(p.getName(), objects[i])))
                        .append(LINE_SEPARATOR);
            }
        }
        builderloggermdc(request, joinPoint);
    }

    /**
     * 设置日志的mdc。链路跟踪
     *
     * @param request   httprequest
     * @param joinPoint 切点
     */
    private void builderloggermdc(HttpServletRequest request, JoinPoint joinPoint) {
        try {
            Gson gson = new Gson();
            String requestId = request.getHeader("X-Request-Id");
            String token = request.getHeader("backend-token");
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            MDC.put("clientIp", request.getRemoteAddr());
            MDC.put("token", token);
            MDC.put("requestId", requestId);
            MDC.put("code", "-");
            MDC.put("requestParams", gson.toJson(Arrays.toString(joinPoint.getArgs())));
            MDC.put("uri", request.getRequestURI());
            MDC.put("queryString", request.getQueryString());
            MDC.put("localIP", request.getLocalAddr());
            String tid = UUID.randomUUID().toString().trim().replaceAll("-", "");
            MDC.put("traceId", tid);
        } catch (Exception e) {
            log.error("builderLoggerMDC error={}", e);
        }
    }

    /**
     * 业务执行完毕后打印日志
     *
     * @param ret ret
     * @throws Throwable Throwable
     */
    @AfterReturning(pointcut = "log()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        StringBuilder builder = SB.get();
        Gson gson = new Gson();
        builder.append("response : " + gson.toJson(ret)).append(LINE_SEPARATOR);
        builder
                .append("spend time : " + (System.currentTimeMillis() - START_TIME.get()) + "ms")
                .append(LINE_SEPARATOR);
        builder
                .append("-----------doAfterReturning----------end-----------------------")
                .append(LINE_SEPARATOR);
        log.info(builder.toString());

        MDC.put("code", HttpStatus.OK.value() + "");

        // 处理完请求,清空MDC
        MDC.clear();
    }

    /**
     * 抛出异常打印日志
     *
     * @param exp exp
     * @throws Throwable Throwable
     */
    @AfterThrowing(pointcut = "log()", throwing = "exp")
    public void doAfterThrowing(Exception exp) throws Throwable {
        // 处理完请求，返回内容
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        StringBuilder builder = new StringBuilder();
        Gson gson = new Gson();
        // 记录下请求内容
        builder.append("-------------doAfterThrowing-------begin------------------------").append(LINE_SEPARATOR);
        builder.append("request url : ").append(request.getRequestURL().toString()).append(LINE_SEPARATOR);
        builder.append("request method: ").append(request.getMethod()).append(LINE_SEPARATOR);
        builder.append("request contentType : ").append(request.getContentType()).append(LINE_SEPARATOR);

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            builder.append("request args : ").append(gson.toJson(ImmutableMap.of(paramName, paramValues)))
                    .append(LINE_SEPARATOR);
        }
        builder.append("spend time : ").append((System.currentTimeMillis() - START_TIME.get())).append("ms")
                .append(LINE_SEPARATOR);
        builder.append("-------------doAfterThrowing--------end-----------------------");
    }
}
