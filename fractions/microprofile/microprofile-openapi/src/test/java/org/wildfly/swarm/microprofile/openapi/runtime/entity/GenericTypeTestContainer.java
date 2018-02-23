/*
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
package org.wildfly.swarm.microprofile.openapi.runtime.entity;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("unused")
public class GenericTypeTestContainer {
    // Nesting generics.
    KustomPair<KustomPair<String, String>, Integer> nesting;

    // More complex nesting of generics including unbounded wildcard
    Fuzz<KustomPair<Fuzz<String, Date>, ?>, Double> complexNesting;

    // Complex inheritance requiring manual resolution of type variables in superclasses
    Foo complexInheritance;

    // Generics with bounds
    KustomPair<? extends Integer, ? super Integer> genericWithBounds;

    // Type containing a variety of collections and maps.
    GenericFieldTestContainer<String, LocalDateTime> genericContainer;
}
