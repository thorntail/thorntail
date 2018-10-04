#!/bin/bash

# maven-scm-publish-plugin feeds sh too many arguments, so we have to
# do this ourselves

site_dir=$1
tmp_dir=$2
version=$3

git clone git@github.com:thorntail/javadocs.git $tmp_dir
cd $tmp_dir
git checkout gh-pages
git pull
rsync -avz $site_dir/ $tmp_dir
git add $version

if grep -q "$version\$" _data/versions.yml; then
    echo "$version exists in _data/versions.yml"
else
    echo -e "\n- $version" >> _data/versions.yml
    git add _data/versions.yml
fi
 
git commit -m "CI generated API documentation for $version"
git push origin gh-pages
