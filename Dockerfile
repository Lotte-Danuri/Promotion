FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/promotion-0.0.1-SNAPSHOT.jar PromotionServer.jar
ENTRYPOINT ["java","-jar","PromotionServer.jar"]