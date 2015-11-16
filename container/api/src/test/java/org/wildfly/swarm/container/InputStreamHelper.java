package org.wildfly.swarm.container;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class InputStreamHelper {

    public static List<String> read(InputStream in) throws IOException {

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add( line.trim() );
            }
        }
        return lines;
    }
}
