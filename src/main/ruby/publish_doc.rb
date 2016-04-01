# maven-scm-publish-plugin feeds sh too many arguments, so we have to
# do this ourselves

git clone git@github.com:wildfly-swarm/wildfly-config-api.git target/scm-publish
cd target/scm-publish && git checkout gh-pages
rsync -avz api/target/site/ api/target/scm-publish
cd api/target/scm-publish && git commit -a -m "CI generated API documentation"
cd api/target/scm-publish && git push origin gh-pages
