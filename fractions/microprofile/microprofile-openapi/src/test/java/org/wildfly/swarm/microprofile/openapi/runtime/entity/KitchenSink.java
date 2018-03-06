/**
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package org.wildfly.swarm.microprofile.openapi.runtime.entity;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.apps.airlines.model.Booking;
import org.eclipse.microprofile.openapi.apps.airlines.model.CreditCard;
import org.eclipse.microprofile.openapi.apps.airlines.model.Flight;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * Kitchen sink.
 */
@Schema(description = "This is the kitchen sink description!",
        example = "This is the KitchenSink example field in Schema",
        deprecated = true)
@SuppressWarnings("unused")
public class KitchenSink {

    // Arrays
    @SuppressWarnings("rawtypes")
    Enum[] bareEnum;

    int[] array;

    @Schema(maximum = "9001")
    int primitiveFoo;

    // TODO
    //int[][] array2d;

    // Handle arrays
    @Schema(required = true)
    Bar[] fooArray = new Bar[2];

    TimerTask[] rawArray = new TimerTask[2];

    Long[] longArray = {1L, 2L};

    // Generic list with super
    List<? super Flight> barSuper;

    // Generic list with extends
    List<? extends Bar> barExtends;

    // Bare collection
    @SuppressWarnings("rawtypes")
    Collection bareCollection;

    // Bare list
    @SuppressWarnings("rawtypes")
    @Schema(required = true)
    List unsafeList;

    // Maps
    @Schema(required = true)
    Map<String, CreditCard> creditCardMap = new LinkedHashMap<>();

    Map<? extends Foo, ? super Foo> blahMap;

    Map<Integer, StringBuffer> awkwardMap;

    Map<Integer, BazEnum> awkwardMap2;

    // OAI TCK fields
    @Schema(required = true)
    Booking booking;

    @Schema(required = true, example = "window", maxLength = 999)
    String seatPreference;

    @Schema(writeOnly = true)
    Integer writeOnlyInteger = 5;

    @Schema(required = true)
    List<CreditCard> ccList;

    // Simple generic typed pair to test handling of pType
    KustomPair<String, Integer> simpleParameterizedType;

    // Nesting generic types with same type.
    KustomPair<KustomPair<String, String>, Integer> nesting;

    // Complex nesting of generics including unbounded wildcard
    Fuzz<KustomPair<Fuzz<String, ?>, Integer>, Double> complexNesting;

    // Handle ? with extends bound
    @Schema(required = true)
    Fuzz<String, ? extends Foo> fuzzListWildcard;

    // Extends and super bounds.
    KustomPair<? extends String, ? super String> customTypeExtendsSuper;

    // Complex generics + inheritance
    Foo foo;

    // Cycles
    BuzzLinkedList rootNode;

    // Handle Void
    Void voidField = null;

    // With format and
    @Schema(type = SchemaType.STRING, format="password")
    String password = "hunter1";

    public KitchenSink(){
    }
}
