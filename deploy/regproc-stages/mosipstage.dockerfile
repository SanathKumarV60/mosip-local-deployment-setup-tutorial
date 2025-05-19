#docker for config server
FROM openjdk:11-oraclelinux7

RUN yum  install -y git unzip wget
COPY registration-processor-common-camel-bridge-1.2.0.2.jar /
COPY runcmd.sh /
CMD ["/bin/sh","runcmd.sh"]
#CMD [ "java", \
# "-jar","registration-processor-common-camel-bridge-1.2.0.2.jar" ]

