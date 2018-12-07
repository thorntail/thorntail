/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.elytron;

import java.util.ArrayList;
import java.util.HashMap;

import org.wildfly.swarm.config.Elytron;
import org.wildfly.swarm.config.elytron.SimplePermissionMapper;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.wildfly.extension.elytron")
@MarshalDMR
public class ElytronFraction extends Elytron<ElytronFraction> implements Fraction<ElytronFraction> {

    private static final String ELYTRON = "elytron";

    private static final String GLOBAL = "global";

    private static final String LOCAL = "local";

    private static final String LOCAL_AUDIT = "local-audit";

    private static final String MANAGEMENT_DOMAIN = "ManagementDomain";

    private static final String MANAGEMENT_REALM = "ManagementRealm";

    private static final String APPLICATION_DOMAIN = "ApplicationDomain";

    private static final String APPLICATION_REALM = "ApplicationRealm";

    private static final String MECHANISM_NAME = "mechanism-name";

    private static final String MECHANISM_REALM_CONFIGURATIONS = "mechanism-realm-configurations";

    private static final String REALM = "realm";

    private static final String REALM_NAME = "realm-name";

    private static final String CLASS_NAME = "class-name";

    private static final String MODULE = "module";

    private static final String TARGET_NAME = "target-name";

    private static final String COMBINED_PROVIDERS = "combined-providers";

    private static final String ROLE_DECODER = "role-decoder";

    private static final String OPENSSL = "openssl";


    @Override
    @SuppressWarnings("unchecked")
    public ElytronFraction applyDefaults() {
        finalProviders(COMBINED_PROVIDERS);
        disallowedProvider("OracleUcrypto");

        providerLoader(ELYTRON, (loader) -> loader.module("org.wildfly.security.elytron"));
        providerLoader(OPENSSL, (loader) -> loader.module("org.wildfly.openssl"));
        aggregateProviders(COMBINED_PROVIDERS, (providers) -> {
            providers.provider(ELYTRON);
            providers.provider(OPENSSL);
        });
        // Local audit.log is no longer created by default, please see https://issues.jboss.org/browse/THORN-2084
        securityDomain(APPLICATION_DOMAIN, (domain) -> {
            domain.defaultRealm(APPLICATION_REALM);
            domain.permissionMapper("default-permission-mapper");
            domain.realm(new HashMap() {{
                put(REALM, APPLICATION_REALM);
                put(ROLE_DECODER, "groups-to-roles");
            }});
            domain.realm(new HashMap() {{
                put(REALM, LOCAL);

            }});
        });
        securityDomain(MANAGEMENT_DOMAIN, (domain) -> {
            domain.defaultRealm(MANAGEMENT_REALM);
            domain.permissionMapper("default-permission-mapper");
            domain.realm(new HashMap() {{
                put(REALM, MANAGEMENT_REALM);
                put(ROLE_DECODER, "groups-to-roles");
            }});
            domain.realm(new HashMap() {{
                put(REALM, LOCAL);
                put("role-mapper", "super-user-mapper");
            }});
        });
        identityRealm(LOCAL, (realm) -> {
            realm.identity("$local");
        });
        /*
        propertiesRealm(APPLICATION_REALM, (realm) -> {
            realm.usersProperty("path", "application-users.properties");
            realm.usersProperty("relative-to", "user.dir");
            realm.usersProperty("digest-realm-name", APPLICATION_REALM);
        });
        propertiesRealm(MANAGEMENT_REALM, (realm) -> {
            realm.usersProperty("path", "mgmt-users.properties");
            realm.usersProperty("relative-to", "user.dir");
            realm.usersProperty("digest-realm-name", MANAGEMENT_REALM);
        });
        */

        // TODO
        customRealm(APPLICATION_REALM, (realm) -> {
            realm.module("org.wildfly.swarm.elytron:runtime");
            realm.className("org.wildfly.swarm.elytron.runtime.Realm");
        });

        customRealm(MANAGEMENT_REALM, (realm) -> {
            realm.module("org.wildfly.swarm.elytron:runtime");
            realm.className("org.wildfly.swarm.elytron.runtime.Realm");
        });

        simplePermissionMapper("default-permission-mapper", mapper -> {
            mapper.mappingMode(SimplePermissionMapper.MappingMode.FIRST);
            mapper.permissionMapping(new HashMap() {{
                put("principals", new ArrayList() {{
                    add("anonymous");
                }});
                put("permission-sets", new ArrayList() {{
                    add(new HashMap() {{
                        put("permission-set", "default-permissions");
                    }});
                }});
            }});
            mapper.permissionMapping(new HashMap() {{
                put("match-all", true);
                put("permission-sets", new ArrayList() {{
                    add(new HashMap() {{
                        put("permission-set", "login-permission");
                    }});
                    add(new HashMap() {{
                        put("permission-set", "default-permissions");
                    }});
                }});
            }});
        });

        permissionSet("login-permission", permissionSet -> {
            permissionSet.permission(new HashMap() {{
                put(CLASS_NAME, "org.wildfly.security.auth.permission.LoginPermission");
            }});
        });
        permissionSet("default-permissions");

        constantRealmMapper(LOCAL, (mapper) -> {
            mapper.realmName(LOCAL);
        });

        simpleRoleDecoder("groups-to-roles", (decoder) -> {
            decoder.attribute("groups");
        });

        constantRoleMapper("super-user-mapper", (mapper) -> {
            mapper.role("SuperUser");
        });

        httpAuthenticationFactory("management-http-authentication", (auth) -> {
            auth.httpServerMechanismFactory(GLOBAL);
            auth.securityDomain(MANAGEMENT_DOMAIN);
            auth.mechanismConfiguration(new HashMap() {{
                put(MECHANISM_NAME, "DIGEST");
                put(MECHANISM_REALM_CONFIGURATIONS, new ArrayList() {{
                    add(new HashMap() {{
                        put(REALM_NAME, MANAGEMENT_REALM);
                    }});
                }});
            }});
        });

        providerHttpServerMechanismFactory(GLOBAL);

        saslAuthenticationFactory("management-sasl-authentication", (auth) -> {
            auth.saslServerFactory("configured");
            auth.securityDomain(MANAGEMENT_DOMAIN);
            auth.mechanismConfiguration(new HashMap() {{
                put(MECHANISM_NAME, "JBOSS-LOCAL-USER");
                put("realm-mapper", LOCAL);
            }});
            auth.mechanismConfiguration(new HashMap() {{
                put(MECHANISM_NAME, "DIGEST-MD5");
                put(MECHANISM_REALM_CONFIGURATIONS, new ArrayList() {{
                    add(new HashMap() {{
                        put(REALM_NAME, MANAGEMENT_REALM);
                    }});
                }});
            }});
        });

        saslAuthenticationFactory("application-sasl-authentication", (auth) -> {
            auth.saslServerFactory("configured");
            auth.securityDomain(APPLICATION_DOMAIN);
            auth.mechanismConfiguration(new HashMap() {{
                put(MECHANISM_NAME, "JBOSS-LOCAL-USER");
                put("realm-mapper", LOCAL);
            }});
            auth.mechanismConfiguration(new HashMap() {{
                put(MECHANISM_NAME, "DIGEST-MD5");
                put(MECHANISM_REALM_CONFIGURATIONS, new ArrayList() {{
                    add(new HashMap() {{
                        put(REALM_NAME, APPLICATION_REALM);
                    }});
                }});
            }});
        });

        providerSaslServerFactory(GLOBAL);

        mechanismProviderFilteringSaslServerFactory(ELYTRON, (filtering) -> {
            filtering.saslServerFactory(GLOBAL);
            filtering.filter(new HashMap() {{
                put("provider-name", "WildFlyElytron");
            }});

        });

        configurableSaslServerFactory("configured", (configurable) -> {
            configurable.saslServerFactory(ELYTRON);
            configurable.property("wildfly.sasl.local-user.default-user", "$local");
        });

        return this;
    }

}
