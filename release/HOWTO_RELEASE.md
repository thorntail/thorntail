# Release the primary repository

* Login to https://ci.wildfly-swarm.io/

* Ensure "thorntail-linux", "thorntail-windows", "thorntail-deploy", "thorntail-examples-linux" and "thorntail-examples-windows" are all passing before continuing with release.

* Trigger a build of https://ci.wildfly-swarm.io/view/Release/job/thorntail-release/build?delay=0sec
    * Specifying release and next version (with -SNAPSHOT suffix), for example, 2.0.0.Final and 2.0.1.Final-SNAPSHOT.

* Wait untill the build updates the master with the next development version and then update `/boms/bom-certified/pom.xml` with this version as well as it doesn't happen in the above command (Maybe add something to CI job to do this?).

* Wait for release to be available in Maven Central before continuing with examples and JIRA releases

# Tag the examples repository

* Checkout/rebase latest from https://github.com/thorntail/thorntail-examples

* Update -examples to just released version, replacing for correct value of `2.0.0.Final`:

        mvn versions:set -DnewVersion=2.0.0.Final

* Update `gradle/build.gradle` and `gradle/pom.xml` to new version

* Then build both regular and uberjar versions

        mvn clean && mvn install

        mvn clean && mvn install -Puberjar

* If successful, commit the version change.

        mvn versions:commit

        git commit -a -m 'Prepare for 2.0.0.Final release'

* And tag it

        git tag 2.0.0.Final

* Then prepare for the next development version:

        mvn versions:set -DnewVersion=2.0.1.Final-SNAPSHOT

* Update `gradle/build.gradle` and `gradle/pom.xml` to next development version

* And commit, and push it all

        mvn versions:commit

        git commit -a -m 'Prepare for next development version'

        git push origin master --tags

# JIRA Releasing

* Go to https://issues.jboss.org/projects/THORN?selectedItem=com.atlassian.jira.jira-projects-plugin:release-page&status=unreleased and select `...` under Actions for the row matching the release we are performing.

* Select `Release` from the drop down.

* Set the current date, which is the date of the release and then click `Release`.

* Open https://issues.jboss.org/issues/?jql=project%20%3D%20THORN%20AND%20status%20%3D%20Resolved%20AND%20fixVersion%20%3D%20EMPTY%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC to find all issues that have been resolved without a fixVersion set.

* Select `Tools` from top right corner, then `all {x} issues` under `Bulk Change`.

* Select all the issues and click `Next`.

* Select `Edit Issues`, `Change Fix Version` set with `Replace all with` and choose our released version from above in drop down. Be sure to de-select `Send mail for this update` at the very bottom of the page before clicking `Next`.

* Review the changes it will make and select `Confirm`. You will need to acknowledge the updates once they're complete.

* Open https://issues.jboss.org/browse/THORN-1409?jql=project%20%3D%20THORN%20AND%20status%20%3D%20Resolved%20AND%20fixVersion%20%3D%202.0.0.Final%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC replacing the version that we're releasing in the query.

* Once again we need to perform a `Bulk Update` of all the issues, which should now include those that we set the fixVersion on in the previous steps.

* Insted of `Edit Issues` we choose `Transition` and select `Resolved -> Closed` as the type of transition.

* Follow the remaining confirmation steps and then acknowledge changes.


# Gather contributors:

Run the local wildfly-swarm/release/fetch-contributors.js with node.js, passing the previous and newly released versions:

    node fetch-contributors.js 2018.5.0 2.0.0.Final

By default the script relies on the following repositories as peers to the core repository:

* `wildfly-swarm.io`
* `thorntail-examples`

If they have different names, simply pass the appropriate name as an argument
to `fetch-contributors` after versions. For example, if `wildfly-swarm.io` was cloned to 'site'
and `thorntail-examples` to 'examples' then run:
    
    node fetch-contributors.js 2018.5.0 2.0.0.Final site examples

# Gather JIRA issues

Run the local wildfly-swarm/release/fetch-notes.js with node.js, passing the version

    node fetch-notes.js 2.0.0.Final

# Update website

Go to the local `wildfly-swarm.io`.

* Prepend the new version to VERSIONS array in `versions.js`
* In `build.js` add redirects to generated docs site at `docs.wildfly-swarm.io` and update `/docs/HEAD` to point to new SNAPSHOT
* Update `src/documentation.adoc`, moving the previous release to the
  previous release section, and changing the current release pointers.

# Blog it all

Create wildfly-swarm.io/src/posts/announce-2.0.0.Final.adoc, replace 2.0.0.Final with the new version.
Copy the fetched contributors and change log into this document.
Make sure at least the breaking changes, as listed in the output of `fetch-notes.js`, are documented in detail.

# Update Che image

* Submit PR for https://github.com/eclipse/che-dockerfiles/tree/master/recipes/centos_wildfly_swarm/Dockerfile to update to latest version


