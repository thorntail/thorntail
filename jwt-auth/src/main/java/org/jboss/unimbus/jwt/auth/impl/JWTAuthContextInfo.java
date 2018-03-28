package org.jboss.unimbus.jwt.auth.impl;


import java.security.interfaces.RSAPublicKey;

/**
 * The public key and expected issuer needed to validate a token.
 */
public class JWTAuthContextInfo {
    private RSAPublicKey signerKey;

    private String issuedBy;

    private int expGracePeriodSecs = 60;

    public JWTAuthContextInfo() {
    }

    public JWTAuthContextInfo(RSAPublicKey signerKey, String issuedBy) {
        this.signerKey = signerKey;
        this.issuedBy = issuedBy;
    }

    public JWTAuthContextInfo(JWTAuthContextInfo orig) {
        this.signerKey = orig.signerKey;
        this.issuedBy = orig.issuedBy;
        this.expGracePeriodSecs = orig.expGracePeriodSecs;
    }

    public RSAPublicKey getSignerKey() {
        return signerKey;
    }

    public void setSignerKey(RSAPublicKey signerKey) {
        this.signerKey = signerKey;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public int getExpGracePeriodSecs() {
        return expGracePeriodSecs;
    }

    public void setExpGracePeriodSecs(int expGracePeriodSecs) {
        this.expGracePeriodSecs = expGracePeriodSecs;
    }
}
