FROM matsumana/debian9-openjdk11

ADD ./build/libs/*.jar /root/app.jar

ENTRYPOINT java \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
    \
    -Djava.rmi.server.hostname=127.0.0.1 \
    -Dcom.sun.management.jmxremote.rmi.port=8686 \
    -Dcom.sun.management.jmxremote.port=8686 \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.authenticate=false \
    \
    -XX:StartFlightRecording=dumponexit=true,filename=/root/flight_recording_$(date +"%Y-%m-%d_%H-%M-%S").jfr,delay=2m,maxsize=512m,settings=profile,path-to-gc-roots=true \
    \
    -Xms128m -Xmx128m \
    \
    -jar /root/app.jar \
    \
    --spring.profiles.active=docker

EXPOSE 8083
EXPOSE 5005
EXPOSE 8686
