/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package io.thorntail.openapi.impl.scanner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import io.thorntail.openapi.impl.util.TypeUtil;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.logging.Logger;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeResolver {

    private static final Logger LOG = Logger.getLogger(TypeResolver.class);

    private Deque<Map<String, Type>> resolutionStack;

    private Type leaf;

    private TypeResolver(Type leaf, Deque<Map<String, Type>> resolutionStack) {
        this.leaf = leaf;
        this.resolutionStack = resolutionStack;
    }

    /**
     * Resolve the type that was used to initially construct this {@link TypeResolver}
     *
     * @return the resolved type (if found)
     */
    public Type resolveType() {
        return getResolvedType(leaf);
    }

    /**
     * Resolve a type against this {@link TypeResolver}'s resolution stack
     *
     * @param fieldType type to resolve
     * @return resolved type (if found)
     */
    public Type getResolvedType(Type fieldType) {
        Type current = TypeUtil.resolveWildcard(fieldType);

        for (Map<String, Type> map : resolutionStack) {
            // Look in next entry map-set.
            if (current.kind() == Type.Kind.TYPE_VARIABLE) {
                current = map.get(current.asTypeVariable().identifier());
            } else if (current.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE) {
                current = map.get(current.asUnresolvedTypeVariable().identifier());
            } else {
                return current;
            }
        }
        return current;
    }

    public static Map<FieldInfo, TypeResolver> getAllFields(IndexView index, Type leaf, ClassInfo leafKlazz) {
        Map<FieldInfo, TypeResolver> fields = new LinkedHashMap<>();
        Type currentType = leaf;
        ClassInfo currentClass = leafKlazz;
        Deque<Map<String, Type>> stack = new ArrayDeque<>();

        do {
            if (currentType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                Map<String, Type> resMap = buildParamTypeResolutionMap(currentClass, currentType.asParameterizedType());
                stack.push(resMap);
            }

            for (FieldInfo field : currentClass.fields()) {
                TypeResolver resolver = new TypeResolver(field.type(), new ArrayDeque<>(stack));
                fields.put(field, resolver);
            }

            currentType = currentClass.superClassType();

            if (currentType == null) {
                break;
            }

            currentClass = getClassByName(index, currentType);

            if (currentClass == null) {
                break;
            }
        } while (currentClass.superClassType() != null);

        return fields;
    }

    private static Map<String, Type> buildParamTypeResolutionMap(ClassInfo klazz, ParameterizedType parameterizedType) {
        List<TypeVariable> typeVariables = klazz.typeParameters();
        List<Type> arguments = parameterizedType.arguments();

        if (arguments.size() != typeVariables.size()) {
            LOG.errorv("Unanticipated mismatch between type arguments and type variables \n" +
                               "Args: {0}\n Vars:{1}", arguments, typeVariables);
        }

        Map<String, Type> resolutionMap = new LinkedHashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            TypeVariable typeVar = typeVariables.get(i);
            Type arg = arguments.get(i);
            resolutionMap.put(typeVar.identifier(), arg);
        }

        return resolutionMap;
    }

    private static ClassInfo getClassByName(IndexView index, @NotNull Type type) {
        return index.getClassByName(TypeUtil.getName(type));
    }

}
