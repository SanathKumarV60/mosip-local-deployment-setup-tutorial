# mosip-local-deployment-setup-tutorial
Howto run MOSIP config server ( which is a Spring Cloud Config Server) 
-----------------------------------------------------------------------
This document shows steps for locally running configurations from one or more branches from git repo (https://github.com/mosip/mosip-config)
Also explains how to create a local docker and run  using docker composer

Setting up configuration repository
1. Clone this repo to a local folder - /mosip/mosip-local-deployment-setup-tutorial
2. Clone or download the required Configuration repository branch from github under config-server folder
   The folder structure would look some thing like this ( Here the config repo folder is renamed with version number postfix)
         /mosip/mosip-local-deployment-setup-tutorial/config-server/mosip-config-1.2.2.0
3. Configuration Server is run as a docker and a dockerfile is provided in config-server folder. Please modify the docker file for any customizations
   
4. mosipconfig.dockerfile
   ==========================
This docker file is the source for creating the docker image. This downloads kernel-config-server-1.1.2.jar from the Maven repo and execute
The folder hierarchy for multiple versions of configurations are as follows:
/mosip-config/{version}/{profile}
The search path is set to /mosip-config as shown below -
spring.cloud.config.server.native.search-locations=file:///mosip-config

Please note the '///' prefix in path. The syntax of file URI is as follows
  file://<host-name>/<path>
  Skipping host-name is squivalent to localhost
  

