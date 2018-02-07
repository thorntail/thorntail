package org.wildfly.swarm.microprofile.openapi.runtime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class Fuzz<T, Q> {
    @Schema(description = "Ah, Q, my favourite variable!")
    Q qValue;

    T tValue;

    Q qAgain;

    T tAgain2;

    Q qAgain3;

    T tAgain4;

}
