/**
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
package org.wildfly.swarm.management;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.wildfly.security.auth.callback.CredentialCallback;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.spec.DigestPasswordAlgorithmSpec;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;

import static org.wildfly.security.password.interfaces.DigestPassword.ALGORITHM_DIGEST_MD5;


/**
 * @author Bob McWhirter
 */
public class AuthCallbackHandler implements CallbackHandler {

    public AuthCallbackHandler(String realm, String userName, String password) {
        this.realm = realm;
        this.userName = userName;
        this.password = password;

    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback current : callbacks) {
            if (current instanceof NameCallback) {
                NameCallback ncb = (NameCallback) current;
                ncb.setName(this.userName);
            } else if (current instanceof RealmCallback) {
                RealmCallback rcb = (RealmCallback) current;
                rcb.setText(rcb.getDefaultText());
            } else if (current instanceof CredentialCallback) {
                CredentialCallback ccb = (CredentialCallback) current;
                try {
                    DigestPasswordAlgorithmSpec algoSpec = new DigestPasswordAlgorithmSpec(this.userName, this.realm);
                    EncryptablePasswordSpec passwordSpec = new EncryptablePasswordSpec(this.password.toCharArray(), algoSpec);
                    Password passwd = PasswordFactory.getInstance(ALGORITHM_DIGEST_MD5).generatePassword(passwordSpec);

                    Credential creds = new PasswordCredential(passwd);
                    ccb.setCredential(creds);
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            } else if ( current instanceof PasswordCallback) {
                PasswordCallback pcb = (PasswordCallback) current;
                pcb.setPassword( this.password.toCharArray() );
            } else {
                throw new UnsupportedCallbackException(current);
            }
        }

    }

    private final String realm;

    private final String userName;

    private final String password;
}
