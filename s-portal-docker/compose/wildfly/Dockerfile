FROM jboss/wildfly:21.0.2.Final

LABEL maintainer d.babinski94@gmail.com
# tworzenie użytkownika
ARG ADMIN_PASSWORD
RUN /opt/jboss/wildfly/bin/add-user.sh admin ${ADMIN_PASSWORD} --silent
ARG SMPORTAL_PASSWORD
RUN /opt/jboss/wildfly/bin/add-user.sh -a smportal ${SMPORTAL_PASSWORD} --silent
# sterowniki JDBC
ADD modules /opt/jboss/wildfly/modules
# plik konfiguracyjny wildfly
ADD standalone-custom.xml /opt/jboss/wildfly/standalone/configuration
# uruchomienie serwera
USER root
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-custom.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
