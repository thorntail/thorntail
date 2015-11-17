package org.wildfly.swarm.plugin.maven;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "run",
requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends StartMojo {
    public RunMojo() {
        waitForProcess = true;
    }
}
