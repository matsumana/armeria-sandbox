FROM bellsoft/liberica-openjdk-debian:17.0.3.1-2

RUN useradd app
RUN mkdir -p /app/log
RUN chown -R app:app /app
USER app

COPY --chown=app:app ./build/libs/BOOT-INF/lib     /app/lib
COPY --chown=app:app ./build/libs/META-INF         /app/META-INF
COPY --chown=app:app ./build/libs/BOOT-INF/lib-app /app/lib-app
COPY --chown=app:app ./build/libs/BOOT-INF/classes /app

CMD ["/bin/bash"]

EXPOSE 8080
