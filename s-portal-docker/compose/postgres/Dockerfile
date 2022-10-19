FROM postgres:13
RUN localedef -i pl_PL -c -f UTF-8 -A /usr/share/locale/locale.alias pl_PL.UTF-8
ENV LANG pl_PL.utf8
#COPY sm-portal.backup /docker-entrypoint-initdb.d/
COPY init.sh /docker-entrypoint-initdb.d/
RUN sed -i -e 's/\r$//' /docker-entrypoint-initdb.d/init.sh