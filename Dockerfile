FROM 161.27.202.100:8103/healtheng/tomcat:8.5-alpine
RUN apk add --no-cache tini

VOLUME /tmp

#copy data folder (prometheus)
COPY deployment/data /usr/local/tomcat/data

#set up tomcat
COPY deployment/tomcat/lib/ /usr/local/tomcat/lib/
COPY deployment/tomcat/exec/ /usr/local/tomcat/bin/

#deploy app
COPY deployment/webapps/axis2 /usr/local/tomcat/webapps/axis2
COPY aar/ /usr/local/tomcat/webapps/axis2/WEB-INF/services/

#fix permissions
RUN chgrp -R 0 /usr/local/tomcat \
&& chmod -R g+w /usr/local/tomcat

#fix windows character
RUN dos2unix /usr/local/tomcat/bin/setenv.sh

EXPOSE 8080
ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "/usr/local/tomcat/bin/catalina.sh run"]