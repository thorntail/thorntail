package org.wildfly.swarm.tools.exec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ExecutableJar implements Executable {

    private final Path jar;

    public ExecutableJar(Path jar) {
        this.jar = jar;
    }

    @Override
    public List<? extends String> toArguments() {
        List<String> args = new ArrayList<>();
        args.add( "-jar" );
        args.add( this.jar.toString() );
        return args;
    }
}
