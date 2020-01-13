FROM java:8
ADD target/gateway-service.jar gateway-service.jar
ENTRYPOINT ["java","-jar","gateway-service.jar"]
