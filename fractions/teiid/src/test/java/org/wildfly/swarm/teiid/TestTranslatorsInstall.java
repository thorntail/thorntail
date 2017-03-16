/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.teiid;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.wildfly.swarm.config.teiid.Translator;

public class TestTranslatorsInstall {

    @SuppressWarnings("rawtypes")
    @Test
    public void testProvidedTranslatorParse() throws IOException {
        List<Translator> translators = new ArrayList<>();
        Properties prop = new Properties();
        InputStream in = TestTranslatorsInstall.class.getClassLoader().getResourceAsStream("provided-translator.properties");
        try {
            prop.load(in);
        } finally{
            in.close();
        }
        prop.forEach((k, v) -> {
            String name = (String) k;
            String module = (String) v;
            String[] array = module.split(":");
            if(array.length == 2){
                translators.add(new Translator(name).module(array[0]).slot(array[1]));        
            } else {
                translators.add(new Translator(name).module(module)); 
            }
        });
        assertEquals(2, translators.size());
    }
}
