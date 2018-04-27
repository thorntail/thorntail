package io.thorntail.jwt.auth.impl;

import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * An abstract CallerPrincipal implementation that provides access to the JWT claims that are required by
 * the microprofile token.
 */
public abstract class JWTCallerPrincipal implements JsonWebToken {
    private String name;

    /**
     * Create a JWTCallerPrincipal with the caller's name
     *
     * @param name - caller's name
     */
    public JWTCallerPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Generate a human readable version of the caller principal and associated JWT.
     *
     * @param showAll - should all claims associated with the JWT be displayed or should only those defined in the
     *                JsonWebToken interface be displayed.
     * @return human readable presentation of the caller principal and associated JWT.
     */
    public abstract String toString(boolean showAll);

    public <T> Optional<T> claim(String claimName) {
        T claim = (T) getClaim(claimName);
        return Optional.ofNullable(claim);
    }
}

