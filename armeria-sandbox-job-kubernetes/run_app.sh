#!/bin/bash

# add Kubernetes cert to Java key store
echo changeit | /root/jre/bin/keytool -import -trustcacerts -file /var/run/secrets/kubernetes.io/serviceaccount/ca.crt -keystore /root/jre/lib/security/cacerts -noprompt

# export token as environment variable
export KUBERNETES_TOKEN=$(cat /run/secrets/kubernetes.io/serviceaccount/token)

/root/jre/bin/java ${JAVA_OPTS} -jar /root/app.jar "$@"
