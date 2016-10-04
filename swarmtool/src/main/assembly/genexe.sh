#!/bin/sh

if [ -z "$1" ]; then
    echo "Usage: $0 path-to-jar"
    exit 1
fi

jar=$1
tmp_jar="${jar}.tmp"

echo "Making ${jar} executable"
ARGS='"$@"'
(echo '#!/bin/sh

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    java="$JAVA_HOME/bin/java"
elif type -p java > /dev/null 2>&1; then
    java=$(type -p java)
elif [ -x "/usr/bin/java" ];  then
    java="/usr/bin/java"
else
    echo "Failed to find Java - please make sure it is in your path or set $JAVA_HOME"
    exit 1
fi

export SWARMTOOL_NAME="$0"
exec "$java" $JAVA_OPTS -jar "$0" "$@"
'; cat $jar) > $tmp_jar

mv $tmp_jar $jar
chmod +x $jar


