FROM openjdk:11
WORKDIR /usr/group_chat_service
COPY build/libs/groupchat_service-0.0.1.jar service.jar
EXPOSE 8082
CMD ["java", "-jar", "service.jar"]