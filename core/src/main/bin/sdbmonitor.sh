#!/bin/bash

current_dir=`pwd`
base_dir=$(cd `dirname $0`; pwd)

# Memory options
MONITOR_HEAP_OPTS="-Xmx256M -Xms256M"
# JVM performance options
MONITOR_JVM_PERFORMANCE_OPTS="-server -Duser.timezone=GMT+08"
# log dir
LOG_DIR="$base_dir/../logs"
# log4j options
MONITOR_LOG4J_OPTS="-Dlogback.configurationFile=file:$base_dir/../conf/logback.xml -Dlogs.dir=$LOG_DIR"
# quartz options
MONITOR_QUARTZ_OPTS="-Dorg.quartz.properties=$base_dir/../conf/quartz.properties"
# program options
MONITOR_DIR_OPTS="-Dprogram.dir=$base_dir/../ -Dcurrent.dir=$current_dir"


if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
fi

for file in $base_dir/../lib/*.jar
do
    CLASSPATH=$CLASSPATH:$file
done

if [ -z "$JAVA_HOME" ]; then
    JAVA="java"
else
    JAVA="$JAVA_HOME/bin/java"
fi
cd ${base_dir}
MAIN_CLASS="com.sequoiadb.monitor.core.Engine"
exec $JAVA $MONITOR_HEAP_OPTS $MONITOR_JVM_PERFORMANCE_OPTS $MONITOR_LOG4J_OPTS $MONITOR_QUARTZ_OPTS $MONITOR_DIR_OPTS -cp $CLASSPATH $MAIN_CLASS "$@"