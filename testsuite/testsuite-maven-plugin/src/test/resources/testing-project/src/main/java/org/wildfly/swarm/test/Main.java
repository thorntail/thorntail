/**
 * Copyright 2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.test;

import org.wildfly.swarm.Swarm;

/*BEGIN:custom main:JAR_WITH_MAIN*/
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;
/*END:custom main:JAR_WITH_MAIN*/

public class Main {
    
    private static Swarm swarm;
    private static Swarm swarm2;

    public static void main(String[] args) throws Exception {
        /*BEGIN:custom main:JAR_WITH_MAIN*/
        swarm = new Swarm();
        WARArchive war = ShrinkWrap.create(WARArchive.class)
                .addPackage(Main.class.getPackage())
                .addAsWebInfResource(new ClassLoaderAsset("web.xml", Main.class.getClassLoader()), WebXmlAsset.NAME);
        swarm.start().deploy(war);
        /*END:custom main:JAR_WITH_MAIN*/
        /*BEGIN:custom main:WAR_WITH_MAIN*/
        swarm2 = new Swarm().start().deploy();
        /*END:custom main:WAR_WITH_MAIN*/
    }

    public static void stopMain() throws Exception {
        swarm.stop();
        swarm2.stop();
    }
}
