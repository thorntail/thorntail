# Release the primary repository

Using Jenkins, run the appropriate release job, specifying 
the release and the next version (with -SNAPSHOT) suffix.

# Tag the examples repository

Once the release is available in central, checkout the
latest -examples repository and run, for correct value
of `2016.9`

    mvn versions:set -DnewVersion=2016.9

Then build both regular and uberjar versions

    mvn clean && mvn install 

    mvn clean && mvn install -Puberjar

If successful, commit the version change.

    git commit -a

And tag it

    git tag 2016.9

Then prepare for the next development version:

    mvn versions:set -DnewVersion=2016.10.0-SNAPSHOT

And commit, and push it all

    git commit -a

    git push origin master


# Tag the documentation

* Edit the `book.json` to ensure all versions are correct,
including Keycloak, WildFly and WildFly Swarm itself.

* `git tag` the repository and `git push --tags`

* Wait for the tagged build to completely build and publish
on GitBooks.  Once we have a linkable build, re-edit
the `book.json` to the next `-SNAPSHOT` version of
WildFly Swarm, commit and push again.

# Gather contributors:

This script assumes that you have `wildfly-swarm-examples`,
`wildfly-swarm-users-guide` and `wildfly-swarm.io` as peers
to the `wildfly-swarm` repository holding this script and that
they are all up-to-date

    node fetch-contributors 2016.8 2016.9

# Gather JIRA issues

run the local fetch-notes.js with node.js, passing the version

    node fetch-notes.js 2016.9

# Update website

* Update the CURRENT_RELEASE variable in `build.js`
* Add a redirect to the above tagged documentation
* Update the documentation page, moving the previous release to the
  previous release section, and changing the current release pointer.

# Blog it all


