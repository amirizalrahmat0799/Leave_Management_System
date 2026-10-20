#!/bin/sh
# Lets context.xml read LEAVE_DB_URL, LEAVE_DB_USER and LEAVE_DB_PASSWORD straight from the environment.
# They are deliberately not passed as -D options: catalina.sh evals CATALINA_OPTS, so the '&' in a JDBC URL
# (or a '$' in a password) would be interpreted by the shell.
CATALINA_OPTS="$CATALINA_OPTS -Dorg.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource"
CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=Asia/Kuala_Lumpur"
export CATALINA_OPTS
