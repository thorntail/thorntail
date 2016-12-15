/*
 * #%L
 * Camel Core :: Tests
 * %%
 * Copyright (C) 2016 RedHat
 * %%
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
 * #L%
 */

package org.wildfly.swarm.camel.core.subA;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.HeaderExpression;
import org.springframework.stereotype.Component;

@Component
public class RouteBuilderA extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer://foo?delay=0&repeatCount=1")
            .setBody(new HeaderExpression(Exchange.TIMER_COUNTER))
            .transform(body().prepend("Hello "))
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    System.out.println("Writing " + exchange + " to " + System.getProperty("jboss.server.data.dir"));
                }
            })
            .to("file://{{jboss.server.data.dir}}/" + RouteBuilderA.class.getName() + "?fileName=fileA&doneFileName=fileA.done");
    }
}
