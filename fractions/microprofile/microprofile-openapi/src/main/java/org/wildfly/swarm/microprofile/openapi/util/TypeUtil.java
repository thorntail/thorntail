package org.wildfly.swarm.microprofile.openapi.util;

import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.PrimitiveType;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeUtil {

    private static final TypeWithFormat STRING_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.NONE);
    private static final TypeWithFormat BYTE_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.BYTE);
    private static final TypeWithFormat CHAR_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.BYTE);
    private static final TypeWithFormat BIGDECIMAL_FORMAT = new TypeWithFormat(SchemaType.NUMBER, DataFormat.NONE);
    private static final TypeWithFormat DOUBLE_FORMAT = new TypeWithFormat(SchemaType.NUMBER, DataFormat.DOUBLE);
    private static final TypeWithFormat FLOAT_FORMAT = new TypeWithFormat(SchemaType.NUMBER, DataFormat.FLOAT);
    private static final TypeWithFormat BIGINTEGER_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.NONE);
    private static final TypeWithFormat INTEGER_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.INT32);
    private static final TypeWithFormat LONG_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.INT64);
    private static final TypeWithFormat SHORT_FORMAT = new TypeWithFormat(SchemaType.INTEGER, DataFormat.NONE);
    private static final TypeWithFormat BOOLEAN_FORMAT = new TypeWithFormat(SchemaType.BOOLEAN, DataFormat.NONE);
    private static final TypeWithFormat ARRAY_FORMAT = new TypeWithFormat(SchemaType.ARRAY, DataFormat.NONE);

    private static final Map<DotName, TypeWithFormat> TYPE_MAP = new LinkedHashMap<>();

    // https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#dataTypeFormat
    static {
        // String
        TYPE_MAP.put(DotName.createSimple(String.class.getName()), STRING_FORMAT);

        // B64 String
        TYPE_MAP.put(DotName.createSimple(Byte.class.getName()), BYTE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Character.class.getName()), CHAR_FORMAT);

        // Decimal
        TYPE_MAP.put(DotName.createSimple(BigDecimal.class.getName()), BIGDECIMAL_FORMAT); // HMM! There is no equivalent format for BigDecimal
        TYPE_MAP.put(DotName.createSimple(Double.class.getName()), DOUBLE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Float.class.getName()), FLOAT_FORMAT);

        // Integer
        TYPE_MAP.put(DotName.createSimple(BigInteger.class.getName()), BIGINTEGER_FORMAT); // HMM! There is no equivalent format for BigInteger
        TYPE_MAP.put(DotName.createSimple(Integer.class.getName()), INTEGER_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Long.class.getName()), LONG_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Short.class.getName()), SHORT_FORMAT); // No equivalent?

        // Boolean
        TYPE_MAP.put(DotName.createSimple(Boolean.class.getName()), BOOLEAN_FORMAT);
    }

    private TypeUtil() {
    }

    // TODO: consider additional checks for Number interface?
    public static TypeWithFormat getTypeFormat(ClassType classType) {
        return Optional
                .ofNullable(TYPE_MAP.get(classType.name()))
                // Otherwise it's some object without a well-known format mapping
                .orElse(new TypeWithFormat(SchemaType.OBJECT, DataFormat.NONE));
    }

    // WIP
    public static TypeWithFormat getTypeFormat(ArrayType arrayType) {
        return ARRAY_FORMAT;
    }

    public static Class<?> getClass(ClassType type) {
        try {
            return Class.forName(type.name().toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static TypeWithFormat getTypeFormat(PrimitiveType.Primitive primitive) {
        switch (primitive) {
            case BYTE:
                return BYTE_FORMAT;
            case CHAR:
                return CHAR_FORMAT;
            case DOUBLE:
                return DOUBLE_FORMAT;
            case FLOAT:
                return FLOAT_FORMAT;
            case INT:
                return INTEGER_FORMAT;
            case LONG:
                return LONG_FORMAT;
            case SHORT:
                return SHORT_FORMAT;
            case BOOLEAN:
                return BOOLEAN_FORMAT;
            default:
                throw new IllegalStateException("Unexpected primitive " + primitive);
        }
    }


    public static final class TypeWithFormat {
        private final SchemaType schemaType;
        private final DataFormat format;

        public TypeWithFormat(@NotNull SchemaType schemaType,
                              @NotNull DataFormat format) {
            this.schemaType = schemaType;
            this.format = format;
        }

        public SchemaType getSchemaType() {
            return schemaType;
        }

        public DataFormat getFormat() {
            return format;
        }
    }

    public enum DataFormat {
        NONE(null),
        INT32("int32"),
        INT64("int64"),
        FLOAT("float"),
        DOUBLE("double"),
        BYTE("byte"),
        BINARY("binary"),
        DATE("date"),
        DATE_TIME("date-time"),
        PASSWORD("password");

        private final String format;

        DataFormat(String format) {
            this.format = format;
        }

        public String format() {
            return format;
        }

        public boolean noFormat() {
            return this == NONE;
        }

        public boolean hasFormat() {
            return this != NONE;
        }
    }
}
