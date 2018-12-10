/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.plugin.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.tools.exec.SwarmProcess;

/**
 * @author Thomas Meyer
 */
@Mojo(name = "stop")
public class StopMojo extends AbstractSwarmMojo {

    @Parameter(defaultValue = "${mojoExecution}")
    protected MojoExecution execution;

    @SuppressWarnings("unchecked")
    @Override
    public void executeSpecific() throws MojoExecutionException, MojoFailureException {
        if (this.execution.getExecutionId().equals("default-cli")) {
            getLog().error("thorntail:stop is not usable from the CLI");
            return;
        }

        List<SwarmProcess> value = (List<SwarmProcess>) getPluginContext().get("thorntail-process");

        if (value == null) {
            getLog().error("No known processes to stop");
            return;
        }

        for (SwarmProcess each : value) {
            stop(each);
        }

        File tmp = (File) getPluginContext().get("thorntail-cp-file");

        if (tmp != null && tmp.exists()) {
            tmp.delete();
        }

        Path tmpDir = new File(System.getProperty("java.io.tmpdir")).toPath();

        if (tmpDir.toFile().exists()) {
            File[] filesTmp = tmpDir.toFile().listFiles();
            for (File tmpFile : filesTmp) {
                Matcher matcher = tempFilePattern.matcher(tmpFile.getName());
                if (matcher.matches()) {
                    TempFileManager.deleteRecursively(tmpFile);
                }
            }
        }
    }

    protected void stop(SwarmProcess process) throws MojoFailureException {
        if (process != null) {
            try {
                process.stop(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new MojoFailureException("unable to stop process", e);
            }
        }
    }

    private static final Pattern tempFilePattern = Pattern.compile("thorntail\\S+[0-9]{5,}.\\S{5,}");
}

