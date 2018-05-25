/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.openapi.runtime.app;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 4/19/18
 */
@Path("/complex")
@Consumes("application/json")
public class ComplexResource {
    @POST
    @Path("/array")
    public CollectionWrapper postArrayOfInts(Integer[] ints) {
        CollectionWrapper result = new CollectionWrapper();
        result.getValues().addAll(Arrays.asList(ints));
        return result;
    }

    @POST
    @Path("/list")
    public CollectionWrapper postArrayOfInts(List<Integer> list) {
        CollectionWrapper result = new CollectionWrapper();
        result.getValues().addAll(list);
        return result;
    }

    public static class CollectionWrapper {
        private Collection<Integer> values = new ArrayList<>();

        public Collection<Integer> getValues() {
            return values;
        }

        public void setValues(Collection<Integer> values) {
            this.values = values;
        }
    }
}
