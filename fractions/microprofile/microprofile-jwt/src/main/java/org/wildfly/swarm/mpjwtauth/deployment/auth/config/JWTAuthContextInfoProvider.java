package org.wildfly.swarm.mpjwtauth.deployment.auth.config;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;

import org.wildfly.swarm.mpjwtauth.deployment.auth.KeyUtils;
import org.wildfly.swarm.mpjwtauth.deployment.principal.JWTAuthContextInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 */
@Dependent
public class JWTAuthContextInfoProvider {
    @Inject
    @ConfigProperty(name = "mpjwt.signerPublicKey")
    private Optional<String> publicKeyPemEnc;
    @Inject
    @ConfigProperty(name = "mpjwt.issuedBy", defaultValue = "NONE")
    private String issuedBy;
    @Inject
    @ConfigProperty(name = "mpjwt.expGracePeriodSecs", defaultValue = "60")
    private Optional<Integer> expGracePeriodSecs;

    @PostConstruct
    void init() {
        System.out.printf("JWTAuthContextInfoProvider.init\n");
    }

    @Produces
    Optional<JWTAuthContextInfo> getOptionalContextInfo() {
        if (!publicKeyPemEnc.isPresent()) {
            return Optional.empty();
        }
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();
        try {
            RSAPublicKey pk = (RSAPublicKey) KeyUtils.decodePublicKey(publicKeyPemEnc.get());
            contextInfo.setSignerKey(pk);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        if (issuedBy != null && !issuedBy.equals("NONE")) {
            contextInfo.setIssuedBy(issuedBy);
        }
        if (expGracePeriodSecs.isPresent()) {
            contextInfo.setExpGracePeriodSecs(expGracePeriodSecs.get());
        }
        return Optional.of(contextInfo);
    }
    @Produces
    JWTAuthContextInfo getContextInfo() {
        return getOptionalContextInfo().get();
    }
}
