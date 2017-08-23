# Release the primary repository

* Login to https://ci.wildfly-swarm.io/

* Trigger a build of https://ci.wildfly-swarm.io/view/Release/job/wildfly-swarm-release/build?delay=0sec
    * Specifying release and next version (with -SNAPSHOT suffix)

* Update next development version in `/boms/bom-certified/pom.xml` as it doesn't happen in the above command (Maybe add something to CI job to do this?).

* Wait for release to be available in Maven Central before continuing with examples releases

# JIRA Releasing

* Go to https://issues.jboss.org/projects/SWARM?selectedItem=com.atlassian.jira.jira-projects-plugin:release-page&status=unreleased and select `...` under Actions for the row matching the release we are performing.

* Set the current date, which is the date of the release and then click `Release`.

* Open https://issues.jboss.org/issues/?jql=project%20%3D%20SWARM%20AND%20status%20%3D%20Resolved%20AND%20fixVersion%20%3D%20EMPTY%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC to find all issues that have been resolved without a fixVersion set.

* Select `Tools` from top right corner, then `all {x} issues` under `Bulk Change`.

* Select all the issues and click `Next`.

* Select `Edit Issues`, `Change Fix Version` set with `Replace all with` and choose our released version from above in drop down. Be sure to de-select `Send mail for this update` at the very bottom of the page before clicking `Next`.

* Review the changes it will make and select `Confirm`. You will need to acknowledge the updates once they're complete.

* Open https://issues.jboss.org/browse/SWARM-1409?jql=project%20%3D%20SWARM%20AND%20status%20%3D%20Resolved%20AND%20fixVersion%20%3D%202017.7.0%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC replacing the version that we're releasing in the query.

* Once again we need to perform a `Bulk Update` of all the issues, which should now include those that we set the fixVersion on in the previous steps.

* Insted of `Edit Issues` we choose `Transition` and select `Resolved -> Closed` as the type of transition.

* Follow the remaining confirmation steps and then acknowledge changes.

# Tag the examples repository

* Checkout/rebase latest from https://github.com/wildfly-swarm/wildfly-swarm-examples

* Update -examples to just released version, replacing for correct value of `2017.2.0`:

        mvn versions:set -DnewVersion=2017.2.0

* Update `gradle/build.gradle` and `gradle/pom.xml` to new version

* Then build both regular and uberjar versions

        mvn clean && mvn install 

        mvn clean && mvn install -Puberjar

* If successful, commit the version change.

        git commit -a -m 'Prepare for 2017.2.0 release'

* And tag it

        git tag 2017.2.0

* Then prepare for the next development version:

        mvn versions:set -DnewVersion=2017.3.0-SNAPSHOT

* Update `gradle/build.gradle` and `gradle/pom.xml` to next development version

* And commit, and push it all

        git commit -a -m 'Prepare for next development version'

        git push origin master --tags


# Tag HowTos

* Checkout/rebase latest from https://github.com/wildfly-swarm/wildfly-swarm-howto

* Update to just released version, replacing for correct value of `2017.2.0-SNAPSHOT` and `2017.2.0`:

        ./release.sh 2017.2.0-SNAPSHOT 2017.2.0

* Edit `book.json` to released version

* Verify a build still works

        mvn clean install

* If successful, commit the version change.

        git commit -a -m 'Prepare for 2017.2.0 release'

* And tag it

        git tag 2017.2.0

* Push changes to doc so GitBooks can build the tagged version

        git push origin master --tags
        
* Wait for the gitbooks build to finish:
   https://www.gitbook.com/@wildfly-swarm

* Then prepare for the next development version:

        ./release.sh 2017.2.0 2017.3.0-SNAPSHOT

* Edit `book.json` to next SNAPSHOT version

* And commit, and push it all

        git commit -a -m 'Prepare for next development version'

        git push origin master


# Update the Reference Guide

In the reference-guide repository (https://github.com/wildfly-swarm/wildfly-swarm-reference-guide)
run the following:

    rm fractions/*
    node update.js

And commit the results if any changes have occurred.  It's okay if git tells
you that nothing has changed.

# Tag the documentation

For each of:

* https://github.com/wildfly-swarm/wildfly-swarm-reference-guide
* https://github.com/wildfly-swarm/wildfly-swarm-users-guide

Do the following:

* Edit the `book.json` to ensure all versions are correct,
including Keycloak, WildFly and WildFly Swarm itself.
    * Reference Guide also requires version to be updated in `update.js`

* Commit the version change

        git commit -a -m 'Prepare for 2017.2.0 release'

* And tag it

        git tag 2017.2.0

* Push changes to doc so GitBooks can build the tagged version

        git push origin master --tags

* Wait for the tagged build to completely build and publish
on GitBooks:
   https://www.gitbook.com/@wildfly-swarm

* Once we have a linkable build, re-edit the `book.json` to the next `-SNAPSHOT` version of
WildFly Swarm, commit and push again.

        git commit -a -m 'Prepare for next development version'

        git push origin master --tags

# Gather contributors:

The script relies on the following repositories as peers to the core repository:

* `wildfly-swarm.io`
* `wildfly-swarm-users-guide`
* `wildfly-swarm-examples`
* `wildfly-swarm-howto`

If they have different names, simply pass the appropriate name as an argument
to `fetch-contributors` after versions in the above order

    node fetch-contributors 2017.1.1 2017.2.0
    node fetch-contributors 2017.1.1 2017.2.0 site users-guide examples howto

# Gather JIRA issues

run the local fetch-notes.js with node.js, passing the version

    node fetch-notes.js 2017.2.0

# Update website

* Prepend the new version to VERSIONS array in `versions.js`
* In `build.js` add redirects to the above tagged documentation
* Update `src/documentation.adoc`, moving the previous release to the
  previous release section, and changing the current release pointers.

# Blog it all

# Update Che image

* Submit PR for https://github.com/eclipse/che-dockerfiles/tree/master/recipes/centos_wildfly_swarm/Dockerfile to update to latest version


