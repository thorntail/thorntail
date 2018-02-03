package org.wildfly.swarm.microprofile.openapi.util;

import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
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
    // SPECIAL FORMATS
    private static final TypeWithFormat ARRAY_FORMAT = new TypeWithFormat(SchemaType.ARRAY, DataFormat.NONE);
    private static final TypeWithFormat OBJECT_FORMAT = new TypeWithFormat(SchemaType.OBJECT, DataFormat.NONE);
    private static final TypeWithFormat DATE_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.DATE);
    private static final TypeWithFormat DATE_TIME_FORMAT = new TypeWithFormat(SchemaType.STRING, DataFormat.DATE_TIME);

    private static final Map<DotName, TypeWithFormat> TYPE_MAP = new LinkedHashMap<>();

    // https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#dataTypeFormat
    static {
        // String
        TYPE_MAP.put(DotName.createSimple(String.class.getName()), STRING_FORMAT);

        // B64 String
        TYPE_MAP.put(DotName.createSimple(Byte.class.getName()), BYTE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(byte.class.getName()), BYTE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Character.class.getName()), CHAR_FORMAT);
        TYPE_MAP.put(DotName.createSimple(char.class.getName()), CHAR_FORMAT);

        // Decimal
        TYPE_MAP.put(DotName.createSimple(BigDecimal.class.getName()), BIGDECIMAL_FORMAT); // HMM! There is no equivalent format for BigDecimal
        TYPE_MAP.put(DotName.createSimple(Double.class.getName()), DOUBLE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(double.class.getName()), DOUBLE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Float.class.getName()), FLOAT_FORMAT);
        TYPE_MAP.put(DotName.createSimple(float.class.getName()), FLOAT_FORMAT);

        // Integer
        TYPE_MAP.put(DotName.createSimple(BigInteger.class.getName()), BIGINTEGER_FORMAT); // HMM! There is no equivalent format for BigInteger
        TYPE_MAP.put(DotName.createSimple(Integer.class.getName()), INTEGER_FORMAT);
        TYPE_MAP.put(DotName.createSimple(int.class.getName()), INTEGER_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Long.class.getName()), LONG_FORMAT);
        TYPE_MAP.put(DotName.createSimple(long.class.getName()), LONG_FORMAT);
        TYPE_MAP.put(DotName.createSimple(Short.class.getName()), SHORT_FORMAT); // No equivalent?
        TYPE_MAP.put(DotName.createSimple(short.class.getName()), SHORT_FORMAT); // No equivalent?

        // Boolean
        TYPE_MAP.put(DotName.createSimple(Boolean.class.getName()), BOOLEAN_FORMAT);
        TYPE_MAP.put(DotName.createSimple(boolean.class.getName()), BOOLEAN_FORMAT);

        // Date
        TYPE_MAP.put(DotName.createSimple(Date.class.getName()), DATE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.sql.Date.class.getName()), DATE_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.LocalDate.class.getName()), DATE_FORMAT);

        // Date Time
        TYPE_MAP.put(DotName.createSimple(java.time.LocalDateTime.class.getName()), DATE_TIME_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.ZonedDateTime.class.getName()), DATE_TIME_FORMAT);
        TYPE_MAP.put(DotName.createSimple(java.time.OffsetDateTime.class.getName()), DATE_TIME_FORMAT);
    }

    private TypeUtil() {
    }

    public static TypeWithFormat getTypeFormat(PrimitiveType primitiveType) {
        return TYPE_MAP.get(primitiveType.name());
    }

    // TODO: consider additional checks for Number interface?
    public static TypeWithFormat getTypeFormat(Type classType) {
        return Optional
                .ofNullable(TYPE_MAP.get(classType.name()))
                // Otherwise it's some object without a well-known format mapping
                .orElse(new TypeWithFormat(SchemaType.OBJECT, DataFormat.NONE));
    }

    public static TypeWithFormat objectFormat() {
        return OBJECT_FORMAT;
    }

    // WIP
    public static TypeWithFormat getTypeFormat(ArrayType arrayType) {
        return ARRAY_FORMAT;
    }

    public static Class<?> getClass(Type type) {
        return getClass(type.name().toString());
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test whether testSubject is an instanceof type test.
     *
     * For example, test whether List is a Collection.
     *
     * Attempts to work with both Jandex and using standard class.
     *
     * @param index Jandex index
     * @param testSubject type to test
     * @param testObject type to test against
     * @return true if is of type
     */
    public static boolean isA(IndexView index, Type testSubject, Type testObject) {
        // First, look in Jandex, as target might not be in our classloader
        ClassInfo jandexKlazz = index.getClassByName(testSubject.name());
        return isA(index, jandexKlazz, testObject);
    }

    public static boolean isA(IndexView index, ClassInfo jandexKlazz, Type testObject) {
        // First, look in Jandex, as target might not be in our classloader
        //ClassInfo jandexKlazz = index.getClassByName(testSubject.name());

        if (jandexKlazz != null) {
            return jandexKlazz.interfaceNames().contains(testObject.name()) || hasSuper(index, jandexKlazz, testObject);
        } else {
            Class<?> subjectKlazz= TypeUtil.getClass(jandexKlazz.name().toString());
            Class<?> objectKlazz = TypeUtil.getClass(testObject);
            return objectKlazz.isAssignableFrom(subjectKlazz);
        }
    }

    private static boolean hasSuper(IndexView index, ClassInfo testSubject, Type testObject) {
        Type superKlazzType = testSubject.superClassType();
        while (superKlazzType != null) {
            if (superKlazzType.equals(testObject)) {
                return true;
            }
            ClassInfo superKlazz = index.getClassByName(superKlazzType.name());
            if (superKlazz == null) {
                Class<?> subjectKlazz= TypeUtil.getClass(testSubject.name().toString());
                Class<?> objectKlazz = TypeUtil.getClass(testObject);
                return objectKlazz.isAssignableFrom(subjectKlazz);
            }
            superKlazzType = superKlazz.superClassType();
        }
        return false;
    }

    public static List<FieldInfo> getAllFields(IndexView index, ClassInfo leaf) {
        List<FieldInfo> fields = new ArrayList<>(leaf.fields());
        ClassInfo currentClass = leaf;
        while (currentClass.superClassType() != null) {
            currentClass = index.getClassByName(currentClass.superClassType().name());
            if (currentClass == null)
                break;
            fields.addAll(currentClass.fields());
        }
        Collections.reverse(fields);
        return fields;
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
