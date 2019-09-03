FROM bellsoft/liberica-openjdk-centos:11.0.4

ADD ./build/libs/*.jar /root/app.jar
ADD ./docker-entrypoint.sh /root/docker-entrypoint.sh

CMD ["/bin/bash"]

EXPOSE 8080
EXPOSE 8686
