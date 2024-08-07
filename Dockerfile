FROM tomcat:10-jdk17

ARG ENV
ARG DRIVER_CLASS_NAME
ARG JDBC_URL
ARG JDBC_USER
ARG JDBC_PASSWORD

ENV ENV=${ENV}
ENV DRIVER_CLASS_NAME=${DRIVER_CLASS_NAME}
ENV JDBC_URL=${JDBC_URL}
ENV JDBC_USER=${JDBC_USER}
ENV JDBC_PASSWORD=${JDBC_PASSWORD}

COPY . /usr/local/tomcat/webapps/myapp

WORKDIR /usr/local/tomcat/webapps/myapp

RUN ./gradlew clean build -x test

RUN cp build/libs/*.war /usr/local/tomcat/webapps/ROOT.war

RUN rm -rf /usr/local/tomcat/webapps/myapp /root/.gradle

EXPOSE 8080

CMD ["catalina.sh", "run"]