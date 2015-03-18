
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.modules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A unique identifier for a module within a module loader.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author Jason T. Greene
 *
 * @apiviz.landmark
 */
public final class ModuleIdentifier implements Serializable {

    private static final long serialVersionUID = 118533026624827995L;

    private static final String DEFAULT_SLOT = "main";

    private final String name;
    private final String slot;

    private final transient int hashCode;

    private static final Field hashField;

    static {
        hashField = AccessController.doPrivileged(new PrivilegedAction<Field>() {
            public Field run() {
                final Field field;
                try {
                    field = ModuleIdentifier.class.getDeclaredField("hashCode");
                    field.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new NoSuchFieldError(e.getMessage());
                }
                return field;
            }
        });
    }

    /**
     * The class path module (only present if booted from a class path).
     */
    public static final ModuleIdentifier CLASSPATH = new ModuleIdentifier("Classpath", DEFAULT_SLOT);

    private ModuleIdentifier(final String name, final String slot) {
        this.name = name;
        this.slot = slot;
        hashCode = calculateHashCode(name, slot);
    }

    private static int calculateHashCode(final String name, final String slot) {
        int h = 17;
        h = 37 * h + name.hashCode();
        h = 37 * h + slot.hashCode();
        return h;
    }

    /**
     * Get the module name.
     *
     * @return the module name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the module version slot.
     *
     * @return the version slot
     */
    public String getSlot() {
        return slot;
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Object other) {
        return other instanceof ModuleIdentifier && equals((ModuleIdentifier)other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(ModuleIdentifier other) {
        return this == other || other != null && name.equals(other.name) && slot.equals(other.slot);
    }

    /**
     * Determine the hash code of this module identifier.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Get the string representation of this module identifier.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        //return escapeName(name) + ":" + escapeSlot(slot);
        return name + ":" + slot;
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        try {
            hashField.setInt(this, calculateHashCode(name, slot));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    private static String escapeName(String name) {
        final StringBuilder b = new StringBuilder();
        boolean escaped = false;
        int c;
        for (int i = 0; i < name.length(); i = name.offsetByCodePoints(i, 1)) {
            c = name.codePointAt(i);
            switch (c) {
                case '\\':
                case ':':
                    escaped = true;
                    b.append('\\');
                    // fall thru
                default:
                    b.append(c);
            }
        }
        return escaped ? b.toString() : name;
    }

    private static String escapeSlot(String slot) {
        final StringBuilder b = new StringBuilder();
        boolean escaped = false;
        int c;
        for (int i = 0; i < slot.length(); i = slot.offsetByCodePoints(i, 1)) {
            c = slot.codePointAt(i);
            switch (c) {
                case '\\':
                    escaped = true;
                    b.append('\\');
                    // fall thru
                default:
                    b.append(c);
            }
        }
        return escaped ? b.toString() : slot;
    }

    /**
     * Parse a module specification from a string.
     *
     * @param moduleSpec the specification string
     * @return the module identifier
     * @throws IllegalArgumentException if the format of the module specification is invalid or it is {@code null}
     */
    public static ModuleIdentifier fromString(String moduleSpec) throws IllegalArgumentException {
        if (moduleSpec == null) {
            throw new IllegalArgumentException("Module specification is null");
        }
        if (moduleSpec.length() == 0) {
            throw new IllegalArgumentException("Empty module specification");
        }

        int c;
        final StringBuilder b = new StringBuilder();
        int i = 0;
        while (i < moduleSpec.length()) {
            c = moduleSpec.codePointAt(i);
            if (c == '\\') {
                b.appendCodePoint(c);
                i = moduleSpec.offsetByCodePoints(i, 1);
                if (i < moduleSpec.length()) {
                    c = moduleSpec.codePointAt(i);
                    b.appendCodePoint(c);
                } else {
                    throw new IllegalArgumentException("Name has an unterminated escape");
                }
            } else if (c == ':') {
                i = moduleSpec.offsetByCodePoints(i, 1);
                if (i == moduleSpec.length()) {
                    throw new IllegalArgumentException("Slot is empty");
                }
                // end of name, now comes the slot
                break;
            } else {
                b.appendCodePoint(c);
            }
            i = moduleSpec.offsetByCodePoints(i, 1);
        }
        final String name = b.toString();
        b.setLength(0);
        if (i < moduleSpec.length()) do {
            c = moduleSpec.codePointAt(i);
            b.appendCodePoint(c);
            i = moduleSpec.offsetByCodePoints(i, 1);
        } while (i < moduleSpec.length()); else {
            return new ModuleIdentifier(name, DEFAULT_SLOT);
        }
        return new ModuleIdentifier(name, b.toString());
    }

    /**
     * Creates a new module identifier using the specified name and slot.
     * A slot allows for multiple modules to exist with the same name.
     * The main usage pattern for this is to differentiate between
     * two incompatible release streams of a module.
     *
     * Normally all module definitions wind up in the "main" slot.
     * An unspecified or null slot will result in placement in the "main"
     * slot.
     *
     * Unless you have a true need for a slot, it should not be specified.
     * When in doubt use the {{@link #create(String)} method instead.
     *
     * @param name the name of the module
     * @param slot the slot this module belongs in
     * @return the identifier
     */
    public static ModuleIdentifier create(final String name, String slot) {
        if (name == null)
            throw new IllegalArgumentException("Name can not be null");

        if (slot == null)
            slot = DEFAULT_SLOT;

        return new ModuleIdentifier(name, slot);
    }

    /**
     * Creates a new module identifier using the specified name.
     *
     * @param name the name of the module
     * @return the identifier
     */
    public static ModuleIdentifier create(String name) {
        return create(name, null);
    }
}

