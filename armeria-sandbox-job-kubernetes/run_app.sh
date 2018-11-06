#!/bin/bash

# add Kubernetes cert to Java key store
echo changeit | keytool -import -trustcacerts -file /var/run/secrets/kubernetes.io/serviceaccount/ca.crt -keystore $JAVA_HOME/lib/security/cacerts -noprompt

# export token as environment variable
export KUBERNETES_TOKEN=$(cat /run/secrets/kubernetes.io/serviceaccount/token)

java ${JAVA_OPTS_DEV} ${JAVA_OPTS} -jar /root/app.jar "$@"
