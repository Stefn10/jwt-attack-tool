# Tomcat 9 with both WARs deployed
# the WARs are built on the host (build-wars.sh copies them into ./wars/)
FROM tomcat:9.0-jdk8-temurin

# Tomcat 9 ships a sample ROOT app, we remove it so it does not interfere
RUN rm -rf /usr/local/tomcat/webapps/*

COPY wars/rva.war      /usr/local/tomcat/webapps/rva.war
COPY wars/jwt-tool.war /usr/local/tomcat/webapps/jwt-tool.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
