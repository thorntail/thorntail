/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package io.thorntail.openapi.impl.io;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities methods for reading information from a Json Tree.
 *
 * @author eric.wittmann@gmail.com
 */
public final class JsonUtil {

    private static final JsonNodeFactory factory = JsonNodeFactory.instance;

    public static ObjectNode objectNode() {
        return factory.objectNode();
    }

    public static ArrayNode arrayNode() {
        return factory.arrayNode();
    }

    /**
     * Constructor.
     */
    private JsonUtil() {
    }

    /**
     * Extract a string property from the given json tree.  Returns null if no
     * property exists or is not a text node.
     *
     * @param node
     * @param propertyName
     */
    public static String stringProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return propertyNode.asText();
        }
        return null;
    }

    /**
     * Sets the value of a property for a given json node.  If the value is null,
     * then the property is not written.
     *
     * @param node
     * @param propertyName
     * @param propertyValue
     */
    public static void stringProperty(ObjectNode node, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.textNode(propertyValue));
    }

    /**
     * Sets the value of a property for a given json node.  If the value is null,
     * then the property is not written.
     *
     * @param node
     * @param propertyName
     * @param propertyValue
     */
    public static <E extends Enum<E>> void enumProperty(ObjectNode node, String propertyName, E propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.textNode(propertyValue.toString()));
    }

    /**
     * Extract a boolean property from the given json tree.  Returns null if no
     * property exists or is not a boolean node.
     *
     * @param node
     * @param propertyName
     */
    public static Boolean booleanProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return propertyNode.asBoolean();
        }
        return null;
    }

    /**
     * Sets the value of a property for a given json node.  If the value is null,
     * then the property is not written.
     *
     * @param node
     * @param propertyName
     * @param propertyValue
     */
    public static void booleanProperty(ObjectNode node, String propertyName, Boolean propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.booleanNode(propertyValue));
    }

    /**
     * Extract a integer property from the given json tree.  Returns null if no
     * property exists or is not a boolean node.
     *
     * @param node
     * @param propertyName
     */
    public static Integer intProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return propertyNode.asInt();
        }
        return null;
    }

    /**
     * Sets the value of a property for a given json node.  If the value is null,
     * then the property is not written.
     *
     * @param node
     * @param propertyName
     * @param propertyValue
     */
    public static void intProperty(ObjectNode node, String propertyName, Integer propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.numberNode(propertyValue));
    }

    /**
     * Extract a BigDecimal property from the given json tree.  Returns null if no
     * property exists or is not a boolean node.
     *
     * @param node
     * @param propertyName
     */
    public static BigDecimal bigDecimalProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return new BigDecimal(propertyNode.asText());
        }
        return null;
    }

    /**
     * Sets the value of a property for a given json node.  If the value is null,
     * then the property is not written.
     *
     * @param node
     * @param propertyName
     * @param propertyValue
     */
    public static void bigDecimalProperty(ObjectNode node, String propertyName, BigDecimal propertyValue) {
        if (propertyValue == null) {
            return;
        }
        if (isIntegerValue(propertyValue)) {
            node.set(propertyName, factory.numberNode(propertyValue.toBigInteger()));
        } else {
            node.set(propertyName, factory.numberNode(propertyValue));
        }
    }

    private static boolean isIntegerValue(BigDecimal bd) {
        return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
    }
}
