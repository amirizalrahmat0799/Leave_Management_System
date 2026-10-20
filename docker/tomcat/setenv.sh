#!/bin/sh
# Passes the database settings from environment variables to context.xml (${leave.db.*}).
CATALINA_OPTS="$CATALINA_OPTS -Dleave.db.url=${LEAVE_DB_URL} -Dleave.db.user=${LEAVE_DB_USER} -Dleave.db.password=${LEAVE_DB_PASSWORD}"
CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=Asia/Kuala_Lumpur"
export CATALINA_OPTS
