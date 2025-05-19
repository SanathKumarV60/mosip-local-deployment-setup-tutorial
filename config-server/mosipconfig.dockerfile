#docker for config server
FROM openjdk:11-oraclelinux7

RUN yum  install -y git unzip wget
#COPY kernel-config-server-1.3.0-beta.1.jar /
#COPY kernel-config-server-1.3.0-beta.1.jar /
RUN wget https://repo1.maven.org/maven2/io/mosip/kernel/kernel-config-server/1.1.2/kernel-config-server-1.1.2.jar
COPY kernel-config-server-1.1.2.jar /

#RUN wget https://github.com/mosip/mosip-config/archive/refs/tags/v1.2.2.0.zip
#RUN unzip v1.2.2.0.zip


RUN mkdir /mosip-config 
COPY mosip-config-1.2.2.0 /mosip-config
CMD [ "java", "-Dspring.profiles.active=native", \
"-Dspring.cloud.config.server.native.search-locations=file:///mosip-config", \
"-Dspring.cloud.config.server.accept-empty=true", \
"-Dspring.cloud.config.server.git.refreshRate=0", \
"-Dlogging.level.org.springframework.cloud.config.server=DEBUG", \
"-Dlogging.file.name=config-server.log", \
 "-jar","kernel-config-server-1.1.2.jar" ]

#"-jar","kernel-config-server-1.3.0-beta.1.jar"]

