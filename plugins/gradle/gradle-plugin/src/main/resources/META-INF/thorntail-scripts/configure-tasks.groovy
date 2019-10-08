import org.gradle.api.tasks.testing.Test

/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// Given our current integration model, we end up in a scenario where we are unable to perform all integrations from
// Java layer as that would mean that we now need to have a dependency on a newer version of the Gradle plugin.
// For such scenarios, we can make use of the concept of applying a Gradle build snippet to the project directly.
// The only catch is that we need to be very careful when going via this approach. Once we figure out a better way
// on integrating directly via Java API calls, we will no longer need this approach.

project.tasks.withType(Test).each { task ->
    // Specify the current Gradle version as an environment variable to all the test tasks. This variable will be used
    // by the Arquillian adapter when invoking the tooling API.
    task.environment('THORNTAIL_ARQUILLIAN_GRADLE_VERSION', gradle.gradleVersion)
}
