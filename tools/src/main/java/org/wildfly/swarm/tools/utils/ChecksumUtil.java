/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.tools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/20/18
 */
public class ChecksumUtil {

    private static final char[] DIGITS = "0123456789abcdef".toCharArray();

    public static String calculateChecksum(InputStream stream) throws IOException, NoSuchAlgorithmException {
        byte[] buf = new byte[1024];
        int len = 0;

        MessageDigest md = MessageDigest.getInstance("SHA1");

        while ((len = stream.read(buf)) >= 0) {
            md.update(buf, 0, len);
        }

        return toHex(md.digest());
    }

    protected static String toHex(byte[] digest) {
        char[] result = new char[digest.length * 2];

        for (int i = 0; i < digest.length; i++) {
            toChars(digest[i], result, i * 2);
        }
        return new String(result);
    }

    private static void toChars(byte b, char[] result, int position) {
        result[position + 1] = DIGITS[b & 0xF]; // hex digit for the last (least significant) 4 bits of the byte
        result[position] = DIGITS[(b >> 4) & 0xF]; // first bits moved to the last, 0xF to handle "negative" numbers
    }

    private ChecksumUtil() {
    }
}
