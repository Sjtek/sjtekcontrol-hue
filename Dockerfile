FROM openjdk:8-jre-alpine
MAINTAINER Wouter Habets (wouterhabets@gmail.com)

ADD build/libs/hue-1.0-SNAPSHOT.jar /opt/sjtekcontrol-hue/sjtekcontrol-hue.jar
CMD ["java", "-jar", "/opt/sjtekcontrol-hue/sjtekcontrol-hue.jar"]