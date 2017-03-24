# Release the primary repository

* Login to https://ci.wildfly-swarm.io/

* Trigger a build of https://ci.wildfly-swarm.io/view/Release/job/wildfly-swarm-release/build?delay=0sec
    * Specifying release and next version (with -SNAPSHOT suffix)

* Wait for release to be available in Maven Central before continuing

# Tag the examples repository

* Checkout/rebase latest from https://github.com/wildfly-swarm/wildfly-swarm-examples

* Update -examples to just released version, replacing for correct value of `2017.2.0`:

        mvn versions:set -DnewVersion=2017.2.0

* Then build both regular and uberjar versions

        mvn clean && mvn install 

        mvn clean && mvn install -Puberjar

* If successful, commit the version change.

        git commit -a -m 'Prepare for 2017.2.0 release'

* And tag it

        git tag 2017.2.0

* Then prepare for the next development version:

        mvn versions:set -DnewVersion=2017.3.0-SNAPSHOT

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


