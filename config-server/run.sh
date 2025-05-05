java "-Dspring.profiles.active=native" \
"-Dspring.cloud.config.server.native.searchLocations=file:///Users/sanathvarambally/Mosip.io/gitrepos/mosip/local-development-setup/config-server/mosip-config-1.2.2.0" \
"-Dspring.cloud.config.server.accept-empty=true" \
"-Dspring.cloud.config.server.git.refreshRate=0" \
"-Dlogging.level.org.springframework.cloud.config.server=All" \
"-Dlogging.file.name=config-server.log" \
"-Dmanagement.endpoints.web.exposure.include=info,health,env,metrics" \
 -jar kernel-config-server-1.0.6.jar


 #curl "http://localhost:51000/config/actuator/health"
