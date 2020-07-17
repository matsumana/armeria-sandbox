FROM ubuntu:bionic-20200630 as builder

RUN apt update && \
    apt install -y curl && \
    curl -L -O https://download.bell-sw.com/java/14.0.2+13/bellsoft-jdk14.0.2+13-linux-amd64.deb && \
    apt install -y -f ./bellsoft-jdk14.0.2+13-linux-amd64.deb

RUN jlink \
    --compress=2 \
    --add-modules=java.base,jdk.unsupported,java.xml,java.desktop,java.sql,jdk.management,jdk.management.agent,jdk.jfr \
    --output=/tmp/jre \
    --bind-services

# --------------------------------
FROM ubuntu:bionic-20200630

RUN useradd app
RUN mkdir -p /app/log
RUN chown -R app:app /app
USER app

COPY --from=builder --chown=app:app /tmp/jre /app/jre

COPY --chown=app:app ./build/libs/BOOT-INF/lib     /app/lib
COPY --chown=app:app ./build/libs/META-INF         /app/META-INF
COPY --chown=app:app ./build/libs/BOOT-INF/lib-app /app/lib-app
COPY --chown=app:app ./build/libs/BOOT-INF/classes /app

ENV JAVA_HOME "/app/jre"
ENV PATH "$JAVA_HOME/bin:$PATH"

CMD ["/bin/bash"]

EXPOSE 8080
