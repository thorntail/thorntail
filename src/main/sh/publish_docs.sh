#!/bin/bash

# maven-scm-publish-plugin feeds sh too many arguments, so we have to
# do this ourselves

site_dir=$1
tmp_dir=$2
version=$3

git clone git@github.com:wildfly-swarm/wildfly-swarm.git $tmp_dir
cd $tmp_dir
git checkout gh-pages
rsync -avz $site_dir/ $tmp_dir
git add $version
git commit -m "CI generated API documentation for $version"
git push origin gh-pages
