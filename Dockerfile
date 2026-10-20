# Build the WAR, then run it on Tomcat 10.1, the classic way to ship a Java web app.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q package -DskipTests \
 && mvn -B -q dependency:copy -Dartifact=com.mysql:mysql-connector-j:8.4.0 -DoutputDirectory=/drivers

FROM tomcat:10.1-jdk17-temurin
# Container-managed connection pools need the JDBC driver in Tomcat's own lib folder
COPY --from=build /drivers/*.jar /usr/local/tomcat/lib/
COPY docker/tomcat/setenv.sh /usr/local/tomcat/bin/setenv.sh
COPY --from=build /src/target/leave.war /usr/local/tomcat/webapps/ROOT.war
RUN chmod +x /usr/local/tomcat/bin/setenv.sh
EXPOSE 8080
