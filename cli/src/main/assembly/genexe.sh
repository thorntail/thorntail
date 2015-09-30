#!/bin/sh
dir="target/bin"
exe="$dir/swarmtool"

if [ ! -d $dir ] ; then
  mkdir -vp $dir
fi

if [ -f $exe ] ; then 
  echo "Removing old swarmtool executable"
  rm -f $exe
fi

echo "Creating swarmtool executable"
ARGS='"$@"'
(echo '#!/bin/sh
exec java -jar "$0" "$@"
'; cat target/wildfly-swarm-cli-standalone.jar) > $exe && chmod +x $exe


