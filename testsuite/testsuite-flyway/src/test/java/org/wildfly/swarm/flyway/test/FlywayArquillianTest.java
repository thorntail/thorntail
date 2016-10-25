/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.flyway.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.WAR)
public class FlywayArquillianTest {

    @Test
    public void testDataSourceContents() throws Exception {
        DataSource dataSource = (DataSource) new InitialContext().lookup("java:jboss/datasources/ExampleDS");
        try (Connection con = dataSource.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) FROM PERSON");
                ResultSet rs = stmt.executeQuery()) {
            Assert.assertTrue("No data found in SQL statement", rs.next());
            Assert.assertEquals("Migration did not perform correctly", 3, rs.getInt(1));
        }
    }
}
