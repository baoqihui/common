FROM openjdk:11-jre-slim
ADD /target/common-0.0.1-SNAPSHOT.jar  /common.jar
EXPOSE 8082
ENTRYPOINT ["java","-Xms1024m","-Xmx1024m","-jar","-Duser.timezone=GMT+08","/common.jar","--spring.profiles.active=dev","-c"]