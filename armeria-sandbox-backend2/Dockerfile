FROM ubuntu:bionic-20191010 as builder

RUN apt update && \
    apt install -y curl && \
    curl -L -O https://download.bell-sw.com/java/13.0.1/bellsoft-jdk13.0.1-linux-amd64.deb && \
    apt install -y -f ./bellsoft-jdk13.0.1-linux-amd64.deb

RUN useradd app
USER app

WORKDIR /tmp
RUN jlink \
    --compress=2 \
    --add-modules=java.base,jdk.unsupported,java.xml,java.desktop,java.instrument,java.sql,jdk.management,jdk.management.agent,jdk.jfr \
    --output=jre \
    --bind-services

ADD ./build/libs/*.jar /tmp/app.jar
RUN jar xvf *.jar
RUN mkdir /tmp/BOOT-INF/lib-app
RUN mv /tmp/BOOT-INF/lib/armeria-sandbox-*.jar /tmp/BOOT-INF/lib-app

# --------------------------------
FROM ubuntu:bionic-20191010

RUN useradd app
RUN mkdir -p /app/log
RUN chown -R app:app /app
USER app

COPY --from=builder /tmp/jre              /app/jre
COPY --from=builder /tmp/BOOT-INF/lib     /app/lib
COPY --from=builder /tmp/META-INF         /app/META-INF
COPY --from=builder /tmp/BOOT-INF/lib-app /app/lib-app
COPY --from=builder /tmp/BOOT-INF/classes /app

ENV JAVA_HOME "/app/jre"
ENV PATH "$JAVA_HOME/bin:$PATH"

CMD ["/bin/bash"]

EXPOSE 8080
