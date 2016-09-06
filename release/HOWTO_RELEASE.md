# Release the primary repository

Using Jenkins, run the appropriate release job, specifying 
the release and the next version (with -SNAPSHOT) suffix.

# Tag the examples repository

Once the release is available in central, checkout the
latest -examples repository and run, for correct value
of `2016.9`

    mvn versions:set -DnewVersion=2016.9

The build both regular and uberjar versions

    mvn clean && mvn install 

    mvn clean && mvn install -Puberjar

# Gather contributors:

git log 2016.8...2016.9 | grep Author | sort | uniq

# Gather JIRA issues

run the local fetch-notes.js with node.js, passing the version

    node fetch-notes.js 2016.9

# Blog it all
