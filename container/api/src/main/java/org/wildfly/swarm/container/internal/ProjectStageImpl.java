package org.wildfly.swarm.container.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wildfly.swarm.spi.api.ProjectStage;

/**
 * @author Heiko Braun
 * @since 07/04/16
 */
public class ProjectStageImpl implements ProjectStage {

    private String name;
    private Map<String, Object> config = new HashMap<>();
    private Map<String,String> properties = new HashMap<>();

    public ProjectStageImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    ProjectStage initialize() {

        for (String key : config.keySet()) {
            StringBuffer buffer = new StringBuffer();
            parse(buffer, null, key, config);

            String[] lines = buffer.toString().split("\n");
            for (String line : lines) {
                int pos = line.indexOf('=');
                String left = line.substring(0, pos);

                // args precendence (java -Dfoo=bar)
                if(null==System.getProperty(left))
                    this.properties.put(left, line.substring(pos + 1, line.length()));
                else
                    this.properties.put(left, System.getProperty(left));
            }
        }
        return this;
    }

    Map<String, Object> getConfig() {
        return config;
    }

    @Override
    public Map<String,String> getProperties() {
        return this.properties;
    }

    private void parse(StringBuffer buffer, String parent, String key, Map<String, Object> config) {

        if(parent!=null && buffer.substring(buffer.length()-1, buffer.length()).equals("\n"))
            buffer.append(parent);

        buffer.append(key);

        Object o = config.get(key);

        if(o instanceof Map) {
            // children: recursive

            buffer.append(".");

            Map<String,Object> child = (Map<String,Object>)o;

            String nextParent = buffer.toString();
            for (String childKey : child.keySet()) {
                parse(buffer, nextParent, childKey, child);
            }
        }
        else if(o instanceof List)  {

            // list values: non-recursive

            List list = (List)o;
            int i = 0;
            String prefix = buffer.toString();
            for (Object item : list) {
                if(i==0)
                    buffer.append("[").append(i).append("]");
                else
                    buffer.append(prefix).append("[").append(i).append("]");
                buffer.append("=").append(item);
                buffer.append("\n");
                i++;
            }
        }
        else
        {
            // simple values: non-recursive
            buffer.append("=").append(o);
            buffer.append("\n");
        }

    }

    @Override
    public String toString() {
        return "ProjectStage{" +
                "name='" + name + '\'' +
                ", config=" + config +
                '}';
    }
}