#!/bin/sh

function join() {
  local IFS=$1
  shift
  echo "$*"
}

MAIN_CLASS=#MAIN_CLASS#

BIN_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ROOT_DIR=$(cd $BIN_DIR/.. && pwd)
LIB_DIR=$ROOT_DIR/lib
APP_DIR=$ROOT_DIR/app

APP_CP=$APP_DIR/*.jar
APP_DEPLOYMENTS=$APP_DIR/*.war

if [ "x$APP_DEPLOYMENTS" != "x" ]; then
  APP_DEPLOYMENTS=$(join ':' $APP_DEPLOYMENTS)
fi

if [ ! -d $APP_DIR ]; then
  echo "APP does not exist"
  if [ -d "$ROOT_DIR/../classes" ]; then
    echo "classes exists"
    APP_DIR=$ROOT_DIR/../classes
    APP_CP=$APP_DIR
    echo "Using classes/ dir"
  fi
fi

CLASSPATH=$(join ':' $LIB_DIR/*.jar $APP_CP)

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

SERVER_OPTS=""

while [ "$#" -gt 0 ]
do
    case "$1" in
      -cp)
          CLASSPATH="$CLASSPATH:$2"
          shift
          ;;
      -classpath)
          CLASSPATH="$CLASSPATH:$2"
          shift
          ;;
      --)
          shift
          break;;
      *)
          SERVER_OPTS="$SERVER_OPTS $1"
          ;;
    esac
    shift
done

if [ "x$THORNTAIL_CONFIG_LOCATION" = "x" ]; then
 THORNTAIL_CONFIG_LOCATION=$ROOT_DIR/conf
else
 THORNTAIL_CONFIG_LOCATION=$THORNTAIL_CONFIG_LOCATION:$ROOT_DIR/conf
fi

export THORNTAIL_CONFIG_LOCATION

SERVER_OPTS="$SERVER_OPTS $JAVA_OPTS -cp $CLASSPATH -Dthorntail.deployments=$APP_DEPLOYMENTS"
exec $JAVA $SERVER_OPTS $MAIN_CLASS $*
