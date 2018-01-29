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

package org.wildfly.swarm.microprofile.openapi.runtime;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.apps.airlines.model.CreditCard;
import org.eclipse.microprofile.openapi.apps.airlines.model.Flight;

import java.util.List;

/**
 * Customised version of {@link org.eclipse.microprofile.openapi.apps.airlines.model.Booking} for testing purposes.
 */
@Schema(description = "This is a Booking2 class description!",
        example = "This is a example in Booking2 of class example",
        deprecated = true)
public class Booking2 {

    @Schema(required = true)
    private Flight departtureFlight;

    @Schema(required = true)
    private Flight returningFlight;

    @Schema(required = true)
    private CreditCard creditCard;

    @Schema(required = true, example = "32126319")
    private String airMiles;

    @Schema(required = true, example = "window", maxLength = 999)
    private String seatPreference;

    @Schema(writeOnly = true)
    public Integer foo = 5;

    @Schema(required = false)
    public int primitiveFoo;

    @Schema(required = true)
    private long inferMeImImportant = 24;

// TODO

// Handle arrays
//    @Schema(required = true)
//    private CreditCard[] creditCardArray = new CreditCard[2];

    // Handle generic type List (collection)
    @Schema(required = true)
    private List<CreditCard2> stringList;

    // Custom generic types
    @Schema(required = true)
    private KustomPair<String, Integer> somePair;

//    // Nesting generic types.
//    KustomPair<KustomPair<String, String>, Integer> blah;
//    Void foo;

// Handle ?
//    @Schema(required = true)
//    private List<? extends CreditCard> creditCardListWildcard;

// Bare collection
//    @Schema(required = true)
//    private List unsafeList;

// Map
//    @Schema(required = true)
//    private Map<String, CreditCard> creditCardMap = new LinkedHashMap<>();

    @Schema(type = SchemaType.STRING, format="password")
    private String password = "hunter1";

    private Booking2(){
    }

    @Schema(required = true)
    public void setAThing(@Schema(required = true) int primitiveFoo) { // do we merge?
        this.primitiveFoo = primitiveFoo;
    }

    public @Schema(required=true) Flight getDeparttureFlight() {
        return departtureFlight;
    }

    public void setDeparttureFlight(Flight departtureFlight) {
        this.departtureFlight = departtureFlight;
    }

    public Flight getReturningFlight() {
        return returningFlight;
    }

    public void setReturningFlight(Flight returningFlight) {
        this.returningFlight = returningFlight;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public String getAirMiles() {
        return airMiles;
    }

    public void setAirMiles(String airMiles) {
        this.airMiles = airMiles;
    }

    public String getSeatPreference() {
        return seatPreference;
    }

    public void setSeatPreference(String seatPreference) {
        this.seatPreference = seatPreference;
    }

}
