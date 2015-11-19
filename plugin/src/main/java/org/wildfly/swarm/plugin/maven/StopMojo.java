/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author Thomas Meyer
 */
@Mojo(name = "stop")
public class StopMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Process process = (Process) getPluginContext().get("swarm-process");
        if(process != null) {
            process.destroy();
            try {
                if ( ! process.waitFor( 10, TimeUnit.SECONDS ) ) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                throw new MojoExecutionException( "Unable to destroy process", e );
            }
        }
        try {
            IOBridge stdout = (IOBridge) getPluginContext().get("swarm-io-stdout");
            if (stdout != null) {
                stdout.close();
            }
            IOBridge stderr = (IOBridge) getPluginContext().get("swarm-io-stderr");
            if (stderr != null) {
                stderr.close();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error closing file stream", e);
        }

    }
}

