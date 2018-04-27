/**
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

package io.thorntail.openapi.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.core.Application;

import io.thorntail.openapi.impl.api.models.PathItemImpl;
import io.thorntail.openapi.impl.api.models.PathsImpl;
import io.thorntail.openapi.impl.api.models.media.DiscriminatorImpl;
import io.thorntail.openapi.impl.api.models.media.EncodingImpl;
import io.thorntail.openapi.impl.api.models.media.MediaTypeImpl;
import io.thorntail.openapi.impl.api.models.parameters.RequestBodyImpl;
import io.thorntail.openapi.impl.api.models.servers.ServerImpl;
import io.thorntail.openapi.impl.api.models.tags.TagImpl;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.PathItem.HttpMethod;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.Scopes;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import io.thorntail.openapi.impl.api.models.ComponentsImpl;
import io.thorntail.openapi.impl.api.models.ExternalDocumentationImpl;
import io.thorntail.openapi.impl.api.models.OpenAPIImpl;
import io.thorntail.openapi.impl.api.models.OperationImpl;
import io.thorntail.openapi.impl.api.models.callbacks.CallbackImpl;
import io.thorntail.openapi.impl.api.models.examples.ExampleImpl;
import io.thorntail.openapi.impl.api.models.headers.HeaderImpl;
import io.thorntail.openapi.impl.api.models.info.ContactImpl;
import io.thorntail.openapi.impl.api.models.info.InfoImpl;
import io.thorntail.openapi.impl.api.models.info.LicenseImpl;
import io.thorntail.openapi.impl.api.models.links.LinkImpl;
import io.thorntail.openapi.impl.api.models.media.ContentImpl;
import io.thorntail.openapi.impl.api.models.media.SchemaImpl;
import io.thorntail.openapi.impl.api.models.parameters.ParameterImpl;
import io.thorntail.openapi.impl.api.models.responses.APIResponseImpl;
import io.thorntail.openapi.impl.api.models.responses.APIResponsesImpl;
import io.thorntail.openapi.impl.api.models.security.OAuthFlowImpl;
import io.thorntail.openapi.impl.api.models.security.OAuthFlowsImpl;
import io.thorntail.openapi.impl.api.models.security.ScopesImpl;
import io.thorntail.openapi.impl.api.models.security.SecurityRequirementImpl;
import io.thorntail.openapi.impl.api.models.security.SecuritySchemeImpl;
import io.thorntail.openapi.impl.api.models.servers.ServerVariableImpl;
import io.thorntail.openapi.impl.api.models.servers.ServerVariablesImpl;
import io.thorntail.openapi.impl.api.util.MergeUtil;
import io.thorntail.openapi.impl.scanner.OpenApiDataObjectScanner;
import io.thorntail.openapi.impl.util.JandexUtil;
import io.thorntail.openapi.impl.util.JandexUtil.JaxRsParameterInfo;
import io.thorntail.openapi.impl.util.JandexUtil.RefType;
import io.thorntail.openapi.impl.util.ModelUtil;

/**
 * Scans a deployment (using the archive and jandex annotation index) for JAX-RS and
 * OpenAPI annotations.  These annotations, if found, are used to generate a valid
 * OpenAPI model.  For reference, see:
 *
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#annotations
 *
 * @author eric.wittmann@gmail.com
 * @author Ken Finnigan
 */
@SuppressWarnings("rawtypes")
@ApplicationScoped
public class AnnotationModelProducer {

    private OpenAPIImpl oai;

    private String currentAppPath = "";

    private String currentResourcePath = "";

    private String[] currentConsumes;

    private String[] currentProduces;

    private SchemaRegistry schemaRegistry = new SchemaRegistry();

    @Inject
    private IndexView index;

    @Inject
    @ConfigProperty(name = OASConfig.SCAN_DISABLE)
    private Optional<Boolean> scanDisabled;

    @Inject
    @ConfigProperty(name = OASConfig.SCAN_PACKAGES)
    private Optional<Set<String>> scanPackages;

    @Inject
    @ConfigProperty(name = OASConfig.SCAN_CLASSES)
    private Optional<Set<String>> scanClasses;

    @Inject
    @ConfigProperty(name = OASConfig.SCAN_EXCLUDE_PACKAGES)
    private Optional<Set<String>> scanExcludePackages;

    @Inject
    @ConfigProperty(name = OASConfig.SCAN_EXCLUDE_CLASSES)
    private Optional<Set<String>> scanExcludeClasses;

    /**
     * Scan the deployment for relevant annotations.  Produces an OpenAPI data model that was
     * built from those found annotations.
     */
    @Produces
    @OpenApiModel(OpenApiModel.ModelType.ANNOTATIONS)
    public OpenAPI scan() {
        if (this.scanDisabled.orElse(false)) {
            OpenApiMessages.MESSAGES.annotationScanningDisabled();
            return null;
        }

        OpenApiMessages.MESSAGES.scanningDeployment();

        // Initialize a new OAI document.  Even if nothing is found, this will be returned.
        oai = new OpenAPIImpl();
        oai.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Get all jax-rs applications and convert them to OAI models (and merge them into a single one)
        this.index.getAllKnownSubclasses(DotName.createSimple(Application.class.getName()))
                .stream()
                .filter(this::filterClasses)
                .forEachOrdered(ci -> oai = MergeUtil.merge(oai, jaxRsApplicationToOpenApi(ci)));

        // TODO find all OpenAPIDefinition annotations at the package level

        // Now find all jax-rs endpoints
        JandexUtil.getJaxRsResourceClasses(this.index)
                .stream()
                .filter(this::filterClasses)
                .forEachOrdered(ci -> processJaxRsResourceClass(oai, ci));

        // Now that all paths have been created, sort them (we don't have a better way to organize them).
        if (oai != null) {
            Paths paths = oai.getPaths();
            if (paths != null) {
                Paths sortedPaths = new PathsImpl();
                TreeSet<String> sortedKeys = new TreeSet<>(paths.keySet());
                for (String pathKey : sortedKeys) {
                    PathItem pathItem = paths.get(pathKey);
                    sortedPaths.addPathItem(pathKey, pathItem);
                }
                sortedPaths.setExtensions(paths.getExtensions());
                oai.setPaths(sortedPaths);
            }
        }

        return oai;
    }

    private boolean filterClasses(ClassInfo classInfo) {
        if (!scanClasses.isPresent() && !scanPackages.isPresent() && !scanExcludeClasses.isPresent() && !scanExcludePackages.isPresent()) {
            return true;
        }

        String path = classInfo.toString();
        String fqcn = path.endsWith(OpenApiConstants.CLASS_SUFFIX) ? path.substring(0, path.lastIndexOf(OpenApiConstants.CLASS_SUFFIX)) : path;
        String packageName = "";
        if (fqcn.contains(".")) {
            int idx = fqcn.lastIndexOf(".");
            packageName = fqcn.substring(0, idx);
        }

        boolean accept;
        // Includes
        if (!scanClasses.isPresent() && !scanPackages.isPresent()) {
            accept = true;
        } else if (scanClasses.isPresent() && !scanPackages.isPresent()) {
            accept = scanClasses.get().contains(fqcn);
        } else if (!scanClasses.isPresent() && scanPackages.isPresent()) {
            accept = scanPackages.get().contains(packageName);
        } else {
            accept = scanClasses.get().contains(fqcn) || scanPackages.get().contains(packageName);
        }
        // Excludes override includes
        if (scanExcludeClasses.isPresent() && scanExcludeClasses.get().contains(fqcn)) {
            accept = false;
        }
        if (scanExcludePackages.isPresent() && scanExcludePackages.get().contains(packageName)) {
            accept = false;
        }
        return accept;
    }

    /**
     * Processes a JAX-RS {@link Application} and creates an {@link OpenAPI} model.  Performs
     * annotation scanning and other processing.  Returns a model unique to that single JAX-RS
     * app.
     *
     * @param applicationClass
     */
    private OpenAPIImpl jaxRsApplicationToOpenApi(ClassInfo applicationClass) {
        OpenAPIImpl oai = new OpenAPIImpl();
        oai.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Get the @ApplicationPath info and save it for later (also support @Path which seems nonstandard but common).
        ////////////////////////////////////////
        AnnotationInstance appPathAnno = JandexUtil.getClassAnnotation(applicationClass, OpenApiConstants.DOTNAME_APPLICATION_PATH);
        if (appPathAnno == null) {
            appPathAnno = JandexUtil.getClassAnnotation(applicationClass, OpenApiConstants.DOTNAME_PATH);
        }
        if (appPathAnno != null) {
            this.currentAppPath = appPathAnno.value().asString();
        } else {
            this.currentAppPath = "/";
        }

        // Get the @OpenAPIDefinition annotation and process it.
        ////////////////////////////////////////
        AnnotationInstance openApiDefAnno = JandexUtil.getClassAnnotation(applicationClass, OpenApiConstants.DOTNAME_OPEN_API_DEFINITION);
        if (openApiDefAnno != null) {
            processDefinition(oai, openApiDefAnno);
        }

        // Process @SecurityScheme annotations
        ////////////////////////////////////////
        List<AnnotationInstance> securitySchemeAnnotations = JandexUtil.getRepeatableAnnotation(applicationClass,
                                                                                                OpenApiConstants.DOTNAME_SECURITY_SCHEME, OpenApiConstants.DOTNAME_SECURITY_SCHEMES);
        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = readSecurityScheme(annotation);
                Components components = ModelUtil.components(oai);
                components.addSecurityScheme(name, securityScheme);
            }
        }

        // Process @Server annotations
        ///////////////////////////////////
        List<AnnotationInstance> serverAnnotations = JandexUtil.getRepeatableAnnotation(applicationClass,
                                                                                        OpenApiConstants.DOTNAME_SERVER, OpenApiConstants.DOTNAME_SERVERS);
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = readServer(annotation);
            oai.addServer(server);
        }

        return oai;
    }

    /**
     * Processing a single JAX-RS resource class (annotated with @Path).
     *
     * @param openApi
     * @param resourceClass
     */
    private void processJaxRsResourceClass(OpenAPIImpl openApi, ClassInfo resourceClass) {
        OpenApiMessages.MESSAGES.processJaxrsResource(resourceClass.simpleName());

        // Set the current resource path.
        AnnotationInstance pathAnno = JandexUtil.getClassAnnotation(resourceClass, OpenApiConstants.DOTNAME_PATH);
        this.currentResourcePath = pathAnno.value().asString();

        // TODO handle the use-case where the resource class extends a base class, and the base class has jax-rs relevant methods and annotations

        // Process @SecurityScheme annotations
        ////////////////////////////////////////
        List<AnnotationInstance> securitySchemeAnnotations = JandexUtil.getRepeatableAnnotation(resourceClass,
                                                                                                OpenApiConstants.DOTNAME_SECURITY_SCHEME, OpenApiConstants.DOTNAME_SECURITY_SCHEMES);
        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = readSecurityScheme(annotation);
                Components components = ModelUtil.components(openApi);
                components.addSecurityScheme(name, securityScheme);
            }
        }

        // Process tags (both declarations and references)
        ////////////////////////////////////////
        Set<String> tagRefs = new HashSet<>();
        AnnotationInstance tagAnno = JandexUtil.getClassAnnotation(resourceClass, OpenApiConstants.DOTNAME_TAG);
        if (tagAnno != null) {
            if (JandexUtil.isRef(tagAnno)) {
                String tagRef = JandexUtil.stringValue(tagAnno, OpenApiConstants.PROP_REF);
                tagRefs.add(tagRef);
            } else {
                Tag tag = readTag(tagAnno);
                if (tag.getName() != null) {
                    openApi.addTag(tag);
                    tagRefs.add(tag.getName());
                }
            }
        }
        AnnotationInstance tagsAnno = JandexUtil.getClassAnnotation(resourceClass, OpenApiConstants.DOTNAME_TAGS);
        if (tagsAnno != null) {
            AnnotationValue tagsArrayVal = tagsAnno.value();
            if (tagsArrayVal != null) {
                AnnotationInstance[] tagsArray = tagsArrayVal.asNestedArray();
                for (AnnotationInstance ta : tagsArray) {
                    if (JandexUtil.isRef(ta)) {
                        String tagRef = JandexUtil.stringValue(ta, OpenApiConstants.PROP_REF);
                        tagRefs.add(tagRef);
                    } else {
                        Tag tag = readTag(ta);
                        if (tag.getName() != null) {
                            openApi.addTag(tag);
                            tagRefs.add(tag.getName());
                        }
                    }
                }
            }

            List<String> listValue = JandexUtil.stringListValue(tagsAnno, OpenApiConstants.PROP_REFS);
            if (listValue != null) {
                tagRefs.addAll(listValue);
            }
        }

        // Now find and process the operation methods
        ////////////////////////////////////////
        for (MethodInfo methodInfo : resourceClass.methods()) {
            AnnotationInstance get = methodInfo.annotation(OpenApiConstants.DOTNAME_GET);
            if (get != null) {
                processJaxRsMethod(openApi, resourceClass, methodInfo, get, HttpMethod.GET, tagRefs);
            }
            AnnotationInstance put = methodInfo.annotation(OpenApiConstants.DOTNAME_PUT);
            if (put != null) {
                processJaxRsMethod(openApi, resourceClass, methodInfo, put, HttpMethod.PUT, tagRefs);
            }
            AnnotationInstance post = methodInfo.annotation(OpenApiConstants.DOTNAME_POST);
            if (post != null) {
                processJaxRsMethod(openApi, resourceClass, methodInfo, post, HttpMethod.POST, tagRefs);
            }
            AnnotationInstance delete = methodInfo.annotation(OpenApiConstants.DOTNAME_DELETE);
            if (delete != null) {
                processJaxRsMethod(openApi, resourceClass, methodInfo, delete, HttpMethod.DELETE, tagRefs);
            }
            AnnotationInstance head = methodInfo.annotation(OpenApiConstants.DOTNAME_HEAD);
            if (head != null) {
                processJaxRsMethod(openApi, resourceClass, methodInfo, head, HttpMethod.HEAD, tagRefs);
            }
            AnnotationInstance options = methodInfo.annotation(OpenApiConstants.DOTNAME_OPTIONS);
            if (options != null) {
                processJaxRsMethod(openApi, resourceClass, methodInfo, options, HttpMethod.OPTIONS, tagRefs);
            }
        }
    }

    /**
     * Process a single JAX-RS method to produce an OpenAPI Operation.
     *
     * @param openApi
     * @param resource
     * @param method
     * @param methodAnno
     * @param methodType
     * @param resourceTags
     */
    private void processJaxRsMethod(OpenAPIImpl openApi, ClassInfo resource, MethodInfo method,
                                    AnnotationInstance methodAnno, HttpMethod methodType, Set<String> resourceTags) {

        OpenApiMessages.MESSAGES.processJaxrsMethod(method.toString());

        // Figure out the path for the operation.  This is a combination of the App, Resource, and Method @Path annotations
        String path;
        if (method.hasAnnotation(OpenApiConstants.DOTNAME_PATH)) {
            AnnotationInstance pathAnno = method.annotation(OpenApiConstants.DOTNAME_PATH);
            String methodPath = pathAnno.value().asString();
            path = makePath(this.currentAppPath, this.currentResourcePath, methodPath);
        } else {
            path = makePath(this.currentAppPath, this.currentResourcePath);
        }

        // Get or create a PathItem to hold the operation
        PathItem pathItem = ModelUtil.paths(openApi).get(path);
        if (pathItem == null) {
            pathItem = new PathItemImpl();
            ModelUtil.paths(openApi).addPathItem(path, pathItem);
        }

        // Figure out the current @Produces and @Consumes (if any)
        currentConsumes = null;
        currentProduces = null;
        AnnotationInstance consumesAnno = method.annotation(OpenApiConstants.DOTNAME_CONSUMES);
        if (consumesAnno == null) {
            consumesAnno = JandexUtil.getClassAnnotation(method.declaringClass(), OpenApiConstants.DOTNAME_CONSUMES);
        }
        AnnotationInstance producesAnno = method.annotation(OpenApiConstants.DOTNAME_PRODUCES);
        if (producesAnno == null) {
            producesAnno = JandexUtil.getClassAnnotation(method.declaringClass(), OpenApiConstants.DOTNAME_PRODUCES);
        }

        if (consumesAnno != null) {
            AnnotationValue annotationValue = consumesAnno.value();
            if (annotationValue != null) {
                currentConsumes = annotationValue.asStringArray();
            } else {
                currentConsumes = OpenApiConstants.DEFAULT_CONSUMES;
            }
        }
        if (producesAnno != null) {
            AnnotationValue annotationValue = producesAnno.value();
            if (annotationValue != null) {
                currentProduces = annotationValue.asStringArray();
            } else {
                currentProduces = OpenApiConstants.DEFAULT_PRODUCES;
            }
        }

        Operation operation = new OperationImpl();

        // Process any @Operation annotation
        /////////////////////////////////////////
        if (method.hasAnnotation(OpenApiConstants.DOTNAME_OPERATION)) {
            AnnotationInstance operationAnno = method.annotation(OpenApiConstants.DOTNAME_OPERATION);
            // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
            if (operationAnno.value(OpenApiConstants.PROP_HIDDEN) != null && operationAnno.value(OpenApiConstants.PROP_HIDDEN).asBoolean()) {
                return;
            }
            // Otherwise, set various bits of meta-data from the values in the @Operation annotation
            operation.setSummary(JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_SUMMARY));
            operation.setDescription(JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_DESCRIPTION));
            operation.setOperationId(JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_OPERATION_ID));
            operation.setDeprecated(JandexUtil.booleanValue(operationAnno, OpenApiConstants.PROP_DEPRECATED));
        }

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        /////////////////////////////////////////
        boolean hasOpTags = false;
        Set<String> tags = new HashSet<>();
        if (method.hasAnnotation(OpenApiConstants.DOTNAME_TAG)) {
            hasOpTags = true;
            AnnotationInstance tagAnno = method.annotation(OpenApiConstants.DOTNAME_TAG);
            if (JandexUtil.isRef(tagAnno)) {
                String tagRef = JandexUtil.stringValue(tagAnno, OpenApiConstants.PROP_REF);
                tags.add(tagRef);
            } else if (JandexUtil.isEmpty(tagAnno)) {
                // Nothing to do here.  The @Tag() was empty.
            } else {
                Tag tag = readTag(tagAnno);
                if (tag.getName() != null) {
                    openApi.addTag(tag);
                    tags.add(tag.getName());
                }
            }
        }
        if (method.hasAnnotation(OpenApiConstants.DOTNAME_TAGS)) {
            hasOpTags = true;
            AnnotationInstance tagsAnno = method.annotation(OpenApiConstants.DOTNAME_TAGS);
            AnnotationValue tagsArrayVal = tagsAnno.value();
            if (tagsArrayVal != null) {
                AnnotationInstance[] tagsArray = tagsArrayVal.asNestedArray();
                for (AnnotationInstance tagAnno : tagsArray) {
                    if (JandexUtil.isRef(tagAnno)) {
                        String tagRef = JandexUtil.stringValue(tagAnno, OpenApiConstants.PROP_REF);
                        tags.add(tagRef);
                    } else {
                        Tag tag = readTag(tagAnno);
                        if (tag.getName() != null) {
                            openApi.addTag(tag);
                            tags.add(tag.getName());
                        }
                    }
                }
            }

            List<String> listValue = JandexUtil.stringListValue(tagsAnno, OpenApiConstants.PROP_REFS);
            if (listValue != null) {
                tags.addAll(listValue);
            }
        }
        if (!hasOpTags) {
            tags.addAll(resourceTags);
        }
        if (!tags.isEmpty()) {
            operation.setTags(new ArrayList<>(tags));
        }

        // Process @Parameter annotations
        /////////////////////////////////////////
        List<AnnotationInstance> parameterAnnotations = JandexUtil.getRepeatableAnnotation(method,
                                                                                           OpenApiConstants.DOTNAME_PARAMETER, OpenApiConstants.DOTNAME_PARAMETERS);
        for (AnnotationInstance annotation : parameterAnnotations) {
            Parameter parameter = readParameter(annotation);
            if (parameter == null) {
                // Param was hidden
                continue;
            }

            AnnotationTarget target = annotation.target();
            // If target is null, then the @Parameter was found wrapped in a @Parameters
            // If the target is METHOD, then the @Parameter is on the method itself
            // If the target is METHOD_PARAMETER, then the @Parameter is on one of the method's arguments (THIS ONE WE CARE ABOUT)
            if (target != null && target.kind() == Kind.METHOD_PARAMETER) {
                In in = parameterIn(target.asMethodParameter());
                parameter.setIn(in);

                // if the Parameter model we read does *NOT* have a Schema at this point, then create one from the method argument's type
                if (!ModelUtil.parameterHasSchema(parameter)) {
                    Type paramType = JandexUtil.getMethodParameterType(method, target.asMethodParameter().position());
                    Schema schema = typeToSchema(paramType);
                    ModelUtil.setParameterSchema(parameter, schema);
                }
            } else {
                // TODO if the @Parameter is on the method, we could perhaps find the one it refers to by name
                // and use its type to create a Schema (if missing)
            }

            operation.addParameter(parameter);
        }
        // Now process any jax-rs parameters that were NOT annotated with @Parameter (do not yet exist in the model)
        List<Type> parameters = method.parameters();
        for (int idx = 0; idx < parameters.size(); idx++) {
            JaxRsParameterInfo paramInfo = JandexUtil.getMethodParameterJaxRsInfo(method, idx);
            if (paramInfo != null && !ModelUtil.operationHasParameter(operation, paramInfo.name)) {
                Type paramType = parameters.get(idx);
                Parameter parameter = new ParameterImpl();
                parameter.setName(paramInfo.name);
                parameter.setIn(paramInfo.in);
                parameter.setRequired(true);
                Schema schema = typeToSchema(paramType);
                parameter.setSchema(schema);
                operation.addParameter(parameter);
            }

        }

        // TODO @Parameter can be located on a field - what does that mean?
        // TODO need to handle the case where we have @BeanParam annotations


        // Process any @RequestBody annotation
        /////////////////////////////////////////
        // note: the @RequestBody annotation can be found on a method argument *or* on the method
        List<AnnotationInstance> requestBodyAnnotations = JandexUtil.getRepeatableAnnotation(method, OpenApiConstants.DOTNAME_REQUEST_BODY, null);
        for (AnnotationInstance annotation : requestBodyAnnotations) {
            RequestBody requestBody = readRequestBody(annotation);
            // TODO if the method argument type is Request, don't generate a Schema!
            if (!ModelUtil.requestBodyHasSchema(requestBody)) {
                Type requestBodyType = null;
                if (annotation.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
                    requestBodyType = JandexUtil.getMethodParameterType(method, annotation.target().asMethodParameter().position());
                } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                    requestBodyType = JandexUtil.getRequestBodyParameterClassType(method);
                }
                if (requestBodyType != null) {
                    Schema schema = typeToSchema(requestBodyType);
                    ModelUtil.setRequestBodySchema(requestBody, schema, currentConsumes);
                }
            }
            operation.setRequestBody(requestBody);
        }
        // If the request body is null, figure it out from the parameters.  Only if the
        // method declares that it @Consumes data
        if (operation.getRequestBody() == null && currentConsumes != null) {
            Type requestBodyType = JandexUtil.getRequestBodyParameterClassType(method);
            if (requestBodyType != null) {
                Schema schema = typeToSchema(requestBodyType);
                if (schema != null) {
                    RequestBody requestBody = new RequestBodyImpl();
                    ModelUtil.setRequestBodySchema(requestBody, schema, currentConsumes);
                    operation.setRequestBody(requestBody);
                }
            }
        }


        // Process @APIResponse annotations
        /////////////////////////////////////////
        List<AnnotationInstance> apiResponseAnnotations = JandexUtil.getRepeatableAnnotation(method,
                                                                                             OpenApiConstants.DOTNAME_API_RESPONSE, OpenApiConstants.DOTNAME_API_RESPONSES);
        for (AnnotationInstance annotation : apiResponseAnnotations) {
            String responseCode = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_RESPONSE_CODE);
            if (responseCode == null) {
                responseCode = APIResponses.DEFAULT;
            }
            APIResponse response = readResponse(annotation);
            APIResponses responses = ModelUtil.responses(operation);
            responses.addApiResponse(responseCode, response);
        }
        // If there are no responses from annotations, try to create a response from the method return value.
        if (operation.getResponses() == null || operation.getResponses().isEmpty()) {
            createResponseFromJaxRsMethod(method, operation);
        }

        // Process @SecurityRequirement annotations
        ///////////////////////////////////////////
        List<AnnotationInstance> securityRequirementAnnotations = JandexUtil.getRepeatableAnnotation(method,
                                                                                                     OpenApiConstants.DOTNAME_SECURITY_REQUIREMENT, OpenApiConstants.DOTNAME_SECURITY_REQUIREMENTS);
        securityRequirementAnnotations.addAll(
                JandexUtil.getRepeatableAnnotation(resource, OpenApiConstants.DOTNAME_SECURITY_REQUIREMENT, OpenApiConstants.DOTNAME_SECURITY_REQUIREMENTS)
        );
        for (AnnotationInstance annotation : securityRequirementAnnotations) {
            SecurityRequirement requirement = readSecurityRequirement(annotation);
            if (requirement != null) {
                operation.addSecurityRequirement(requirement);
            }
        }

        // Process @Callback annotations
        /////////////////////////////////////////
        List<AnnotationInstance> callbackAnnotations = JandexUtil.getRepeatableAnnotation(method,
                                                                                          OpenApiConstants.DOTNAME_CALLBACK, OpenApiConstants.DOTNAME_CALLBACKS);
        Map<String, Callback> callbacks = new LinkedHashMap<>();
        for (AnnotationInstance annotation : callbackAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                callbacks.put(name, readCallback(annotation));
            }

            if (!callbacks.isEmpty()) {
                operation.setCallbacks(callbacks);
            }
        }

        // Process @Server annotations
        ///////////////////////////////////
        List<AnnotationInstance> serverAnnotations = JandexUtil.getRepeatableAnnotation(method,
                                                                                        OpenApiConstants.DOTNAME_SERVER, OpenApiConstants.DOTNAME_SERVERS);
        if (serverAnnotations.isEmpty()) {
            serverAnnotations.addAll(JandexUtil.getRepeatableAnnotation(method.declaringClass(),
                                                                        OpenApiConstants.DOTNAME_SERVER, OpenApiConstants.DOTNAME_SERVERS));
        }
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = readServer(annotation);
            operation.addServer(server);
        }

        // Now set the operation on the PathItem as appropriate based on the Http method type
        ///////////////////////////////////////////
        switch (methodType) {
            case DELETE:
                pathItem.setDELETE(operation);
                break;
            case GET:
                pathItem.setGET(operation);
                break;
            case HEAD:
                pathItem.setHEAD(operation);
                break;
            case OPTIONS:
                pathItem.setOPTIONS(operation);
                break;
            case PATCH:
                pathItem.setPATCH(operation);
                break;
            case POST:
                pathItem.setPOST(operation);
                break;
            case PUT:
                pathItem.setPUT(operation);
                break;
            case TRACE:
                pathItem.setTRACE(operation);
                break;
            default:
                break;
        }
    }

    /**
     * Called when a jax-rs method's APIResponse annotations have all been processed but
     * no response was actually created for the operation.  This method will create a response
     * from the method information and add it to the given operation.  It will try to do this
     * by examining the method's return value and the type of operation (GET, PUT, POST, DELETE).
     *
     * If there is a return value of some kind (a non-void return type) then the response code
     * is assumed to be 200.
     *
     * If there not a return value (void return type) then either a 201 or 204 is returned,
     * depending on the type of request.
     *
     * TODO generate responses for each checked exception?
     *
     * @param method
     * @param operation
     */
    private void createResponseFromJaxRsMethod(MethodInfo method, Operation operation) {
        Type returnType = method.returnType();

        Schema schema;
        APIResponses responses;
        APIResponse response;
        ContentImpl content;

        if (returnType.kind() == Type.Kind.VOID) {
            String code = "204";
            if (method.hasAnnotation(OpenApiConstants.DOTNAME_POST)) {
                code = "201";
            }
            responses = ModelUtil.responses(operation);
            response = new APIResponseImpl();
            responses.addApiResponse(code, response);
        } else {
            schema = typeToSchema(returnType);
            responses = ModelUtil.responses(operation);
            response = new APIResponseImpl();
            content = new ContentImpl();
            String[] produces = this.currentProduces;
            if (produces == null || produces.length == 0) {
                produces = OpenApiConstants.DEFAULT_PRODUCES;
            }
            for (String producesType : produces) {
                MediaType mt = new MediaTypeImpl();
                mt.setSchema(schema);
                content.addMediaType(producesType, mt);
            }
            response.setContent(content);
            responses.addApiResponse("200", response);
        }
    }

    /**
     * Converts a jandex type to a {@link Schema} model.
     *
     * @param type
     */
    private Schema typeToSchema(Type type) {
        Schema schema = null;
        if (type.kind() == Type.Kind.CLASS) {
            schema = introspectClassToSchema(type.asClassType(), true);
        } else if (type.kind() == Type.Kind.PRIMITIVE) {
            schema = OpenApiDataObjectScanner.process(type.asPrimitiveType());
        } else {
            schema = OpenApiDataObjectScanner.process(index, type);
        }
        return schema;
    }

    /**
     * Determines where an @Parameter can be found (examples include Query, Path,
     * Header, Cookie, etc).
     */
    private In parameterIn(MethodParameterInfo paramInfo) {
        MethodInfo method = paramInfo.method();
        short paramPosition = paramInfo.position();
        List<AnnotationInstance> annotations = JandexUtil.getParameterAnnotations(method, paramPosition);
        for (AnnotationInstance annotation : annotations) {
            if (annotation.name().equals(OpenApiConstants.DOTNAME_QUERY_PARAM)) {
                return In.QUERY;
            }
            if (annotation.name().equals(OpenApiConstants.DOTNAME_PATH_PARAM)) {
                return In.PATH;
            }
            if (annotation.name().equals(OpenApiConstants.DOTNAME_HEADER_PARAM)) {
                return In.HEADER;
            }
            if (annotation.name().equals(OpenApiConstants.DOTNAME_COOKIE_PARAM)) {
                return In.COOKIE;
            }
        }
        return null;
    }

    /**
     * Make a path out of a number of path segments.
     *
     * @param segments
     */
    protected static String makePath(String... segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (segment.startsWith("/")) {
                segment = segment.substring(1);
            }
            if (segment.endsWith("/")) {
                segment = segment.substring(0, segment.length() - 1);
            }
            if (segment.isEmpty()) {
                continue;
            }
            builder.append("/");
            builder.append(segment);
        }
        String rval = builder.toString();
        if (rval.isEmpty()) {
            return "/";
        }
        return rval;
    }

    /**
     * Reads a OpenAPIDefinition annotation.
     *
     * @param openApi
     * @param definitionAnno
     */
    protected void processDefinition(OpenAPIImpl openApi, AnnotationInstance definitionAnno) {
        OpenApiMessages.MESSAGES.processingAnnotation("@OpenAPIDefinition");
        openApi.setInfo(readInfo(definitionAnno.value(OpenApiConstants.PROP_INFO)));
        openApi.setTags(readTags(definitionAnno.value(OpenApiConstants.PROP_TAGS)));
        openApi.setServers(readServers(definitionAnno.value(OpenApiConstants.PROP_SERVERS)));
        openApi.setSecurity(readSecurity(definitionAnno.value(OpenApiConstants.PROP_SECURITY)));
        openApi.setExternalDocs(readExternalDocs(definitionAnno.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(readComponents(definitionAnno.value(OpenApiConstants.PROP_COMPONENTS)));
    }

    /**
     * Reads an Info annotation.
     *
     * @param infoAnno
     */
    private Info readInfo(AnnotationValue infoAnno) {
        if (infoAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Info");
        AnnotationInstance nested = infoAnno.asNested();
        InfoImpl info = new InfoImpl();
        info.setTitle(JandexUtil.stringValue(nested, OpenApiConstants.PROP_TITLE));
        info.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        info.setTermsOfService(JandexUtil.stringValue(nested, OpenApiConstants.PROP_TERMS_OF_SERVICE));
        info.setContact(readContact(nested.value(OpenApiConstants.PROP_CONTACT)));
        info.setLicense(readLicense(nested.value(OpenApiConstants.PROP_LICENSE)));
        info.setVersion(JandexUtil.stringValue(nested, OpenApiConstants.PROP_VERSION));
        return info;
    }

    /**
     * Reads an Contact annotation.
     *
     * @param contactAnno
     */
    private Contact readContact(AnnotationValue contactAnno) {
        if (contactAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Contact");
        AnnotationInstance nested = contactAnno.asNested();
        ContactImpl contact = new ContactImpl();
        contact.setName(JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME));
        contact.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        contact.setEmail(JandexUtil.stringValue(nested, OpenApiConstants.PROP_EMAIL));
        return contact;
    }

    /**
     * Reads an License annotation.
     *
     * @param licenseAnno
     */
    private License readLicense(AnnotationValue licenseAnno) {
        if (licenseAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@License");
        AnnotationInstance nested = licenseAnno.asNested();
        LicenseImpl license = new LicenseImpl();
        license.setName(JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME));
        license.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return license;
    }

    /**
     * Reads any Tag annotations.  The annotation
     * value is an array of Tag annotations.
     *
     * @param tagAnnos
     */
    private List<Tag> readTags(AnnotationValue tagAnnos) {
        if (tagAnnos == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Tag");
        AnnotationInstance[] nestedArray = tagAnnos.asNestedArray();
        List<Tag> tags = new ArrayList<>();
        for (AnnotationInstance tagAnno : nestedArray) {
            if (!JandexUtil.isRef(tagAnno)) {
                tags.add(readTag(tagAnno));
            }
        }
        return tags;
    }

    /**
     * Reads a single Tag annotation.
     *
     * @param tagAnno
     */
    private Tag readTag(AnnotationInstance tagAnno) {
        if (tagAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Tag");
        TagImpl tag = new TagImpl();
        tag.setName(JandexUtil.stringValue(tagAnno, OpenApiConstants.PROP_NAME));
        tag.setDescription(JandexUtil.stringValue(tagAnno, OpenApiConstants.PROP_DESCRIPTION));
        tag.setExternalDocs(readExternalDocs(tagAnno.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        return tag;
    }

    /**
     * Reads any Server annotations.  The annotation value is an array of Server annotations.
     *
     * @param serverAnnos
     */
    private List<Server> readServers(AnnotationValue serverAnnos) {
        if (serverAnnos == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Server");
        AnnotationInstance[] nestedArray = serverAnnos.asNestedArray();
        List<Server> servers = new ArrayList<>();
        for (AnnotationInstance serverAnno : nestedArray) {
            servers.add(readServer(serverAnno));
        }
        return servers;
    }

    /**
     * Reads a single Server annotation.
     */
    private Server readServer(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readServer(value.asNested());
    }

    /**
     * Reads a single Server annotation.
     *
     * @param serverAnno
     */
    private Server readServer(AnnotationInstance serverAnno) {
        if (serverAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Server");
        ServerImpl server = new ServerImpl();
        server.setUrl(JandexUtil.stringValue(serverAnno, OpenApiConstants.PROP_URL));
        server.setDescription(JandexUtil.stringValue(serverAnno, OpenApiConstants.PROP_DESCRIPTION));
        server.setVariables(readServerVariables(serverAnno.value(OpenApiConstants.PROP_VARIABLES)));
        return server;
    }

    /**
     * Reads an array of ServerVariable annotations, returning a new {@link ServerVariables} model.  The
     * annotation value is an array of ServerVariable annotations.
     *
     * @return
     */
    private ServerVariables readServerVariables(AnnotationValue serverVariableAnnos) {
        if (serverVariableAnnos == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@ServerVariable");
        AnnotationInstance[] nestedArray = serverVariableAnnos.asNestedArray();
        ServerVariables variables = new ServerVariablesImpl();
        for (AnnotationInstance serverVariableAnno : nestedArray) {
            String name = JandexUtil.stringValue(serverVariableAnno, OpenApiConstants.PROP_NAME);
            if (name != null) {
                variables.addServerVariable(name, readServerVariable(serverVariableAnno));
            }
        }
        return variables;
    }

    /**
     * Reads a single ServerVariable annotation.
     *
     * @param serverVariableAnno
     */
    private ServerVariable readServerVariable(AnnotationInstance serverVariableAnno) {
        if (serverVariableAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@ServerVariable");
        ServerVariable variable = new ServerVariableImpl();
        variable.setDescription(JandexUtil.stringValue(serverVariableAnno, OpenApiConstants.PROP_DESCRIPTION));
        variable.setEnumeration(JandexUtil.stringListValue(serverVariableAnno, OpenApiConstants.PROP_ENUMERATION));
        variable.setDefaultValue(JandexUtil.stringValue(serverVariableAnno, OpenApiConstants.PROP_DEFAULT_VALUE));
        return variable;
    }

    /**
     * Reads any SecurityRequirement annotations.  The annotation value is an array of
     * SecurityRequirement annotations.
     */
    private List<SecurityRequirement> readSecurity(AnnotationValue securityRequirementAnnos) {
        if (securityRequirementAnnos == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@SecurityRequirement");
        AnnotationInstance[] nestedArray = securityRequirementAnnos.asNestedArray();
        List<SecurityRequirement> requirements = new ArrayList<>();
        for (AnnotationInstance requirementAnno : nestedArray) {
            SecurityRequirement requirement = readSecurityRequirement(requirementAnno);
            if (requirement != null) {
                requirements.add(requirement);
            }
        }
        return requirements;
    }

    /**
     * Reads a single SecurityRequirement annotation.
     *
     * @param annotation
     */
    private SecurityRequirement readSecurityRequirement(AnnotationInstance annotation) {
        String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
        if (name != null) {
            List<String> scopes = JandexUtil.stringListValue(annotation, OpenApiConstants.PROP_SCOPES);
            SecurityRequirement requirement = new SecurityRequirementImpl();
            if (scopes == null) {
                requirement.addScheme(name);
            } else {
                requirement.addScheme(name, scopes);
            }
            return requirement;
        }
        return null;
    }

    /**
     * Reads an ExternalDocumentation annotation.
     *
     * @param externalDocAnno
     */
    private ExternalDocumentation readExternalDocs(AnnotationValue externalDocAnno) {
        if (externalDocAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@ExternalDocumentation");
        AnnotationInstance nested = externalDocAnno.asNested();
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        externalDoc.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return externalDoc;
    }

    /**
     * Reads any Components annotations.
     *
     * @param componentsAnno
     */
    private Components readComponents(AnnotationValue componentsAnno) {
        if (componentsAnno == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Components");
        AnnotationInstance nested = componentsAnno.asNested();
        Components components = new ComponentsImpl();
        // TODO for EVERY item below, handle the case where the annotation is ref-only.  then strip the ref path and use the final segment as the name
        components.setCallbacks(readCallbacks(nested.value(OpenApiConstants.PROP_CALLBACKS)));
        components.setExamples(readExamples(nested.value(OpenApiConstants.PROP_EXAMPLES)));
        components.setHeaders(readHeaders(nested.value(OpenApiConstants.PROP_HEADERS)));
        components.setLinks(readLinks(nested.value(OpenApiConstants.PROP_LINKS)));
        components.setParameters(readParameters(nested.value(OpenApiConstants.PROP_PARAMETERS)));
        components.setRequestBodies(readRequestBodies(nested.value(OpenApiConstants.PROP_REQUEST_BODIES)));
        components.setResponses(readResponses(nested.value(OpenApiConstants.PROP_RESPONSES)));
        components.setSchemas(readSchemas(nested.value(OpenApiConstants.PROP_SCHEMAS)));
        components.setSecuritySchemes(readSecuritySchemes(nested.value(OpenApiConstants.PROP_SECURITY_SCHEMES)));
        return components;
    }

    /**
     * Reads a map of Callback annotations.
     *
     * @param value
     */
    private Map<String, Callback> readCallbacks(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Callback");
        Map<String, Callback> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readCallback(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Callback annotation into a model.
     *
     * @param annotation
     */
    private Callback readCallback(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Callback");
        Callback callback = new CallbackImpl();
        callback.setRef(JandexUtil.refValue(annotation, RefType.Callback));
        String expression = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_CALLBACK_URL_EXPRESSION);
        callback.put(expression, readCallbackOperations(annotation.value(OpenApiConstants.PROP_OPERATIONS)));
        return callback;
    }

    /**
     * Reads the CallbackOperation annotations as a PathItem.  The annotation value
     * in this case is an array of CallbackOperation annotations.
     *
     * @param value
     */
    private PathItem readCallbackOperations(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@CallbackOperation");
        AnnotationInstance[] nestedArray = value.asNestedArray();
        PathItem pathItem = new PathItemImpl();
        for (AnnotationInstance operationAnno : nestedArray) {
            String method = JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_METHOD);
            Operation operation = readCallbackOperation(operationAnno);
            if (method == null) {
                continue;
            }
            try {
                PropertyDescriptor descriptor = null;
                PropertyDescriptor[] descriptors;
                try {
                    descriptors = Introspector.getBeanInfo(pathItem.getClass()).getPropertyDescriptors();
                    for (PropertyDescriptor propDesc : descriptors) {
                        if (propDesc.getName().equals(method.toUpperCase())) {
                            descriptor = propDesc;
                            break;
                        }
                    }
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
                Method mutator = descriptor.getWriteMethod();
                mutator.invoke(pathItem, operation);
            } catch (Exception e) {
                OpenApiMessages.MESSAGES.errorReadingAnnotation("CallbackOperation", e);
            }
        }
        return pathItem;
    }

    /**
     * Reads a single CallbackOperation annotation.
     *
     * @return
     */
    private Operation readCallbackOperation(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@CallbackOperation");
        Operation operation = new OperationImpl();
        operation.setSummary(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SUMMARY));
        operation.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        operation.setExternalDocs(readExternalDocs(annotation.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        operation.setParameters(readCallbackOperationParameters(annotation.value(OpenApiConstants.PROP_PARAMETERS)));
        operation.setRequestBody(readRequestBody(annotation.value(OpenApiConstants.PROP_REQUEST_BODY)));
        operation.setResponses(readCallbackOperationResponses(annotation.value(OpenApiConstants.PROP_RESPONSES)));
        operation.setSecurity(readSecurity(annotation.value(OpenApiConstants.PROP_SECURITY)));
        operation.setExtensions(readExtensions(annotation.value(OpenApiConstants.PROP_EXTENSIONS)));
        return operation;
    }

    /**
     * Reads an array of Parameter annotations into a list.
     *
     * @param value
     */
    private List<Parameter> readCallbackOperationParameters(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Parameter");
        List<Parameter> parameters = new ArrayList<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            parameters.add(readParameter(nested));
        }
        return parameters;
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     *
     * @param value
     */
    private APIResponses readCallbackOperationResponses(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@APIResponse");
        APIResponses responses = new APIResponsesImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String responseCode = JandexUtil.stringValue(nested, OpenApiConstants.PROP_RESPONSE_CODE);
            if (responseCode != null) {
                responses.put(responseCode, readResponse(nested));
            }
        }
        return responses;
    }

    /**
     * Reads a map of Example annotations.
     *
     * @param value
     */
    private Map<String, Example> readExamples(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@ExampleObject");
        Map<String, Example> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readExample(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Example annotation into a model.
     *
     * @param annotation
     */
    private Example readExample(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@ExampleObject");
        Example example = new ExampleImpl();
        example.setSummary(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SUMMARY));
        example.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        example.setValue(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_VALUE));
        example.setExternalValue(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXTERNAL_VALUE));
        example.setRef(JandexUtil.refValue(annotation, RefType.Example));
        return example;
    }

    /**
     * Reads a map of Header annotations.
     *
     * @param value
     */
    private Map<String, Header> readHeaders(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Header");
        Map<String, Header> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readHeader(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Header annotation into a model.
     *
     * @param annotation
     */
    private Header readHeader(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Header");
        Header header = new HeaderImpl();
        header.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        header.setSchema(readSchema(annotation.value(OpenApiConstants.PROP_SCHEMA)));
        header.setRequired(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_REQUIRED));
        header.setDeprecated(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_DEPRECATED));
        header.setAllowEmptyValue(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        header.setRef(JandexUtil.refValue(annotation, RefType.Header));
        return header;
    }

    /**
     * Reads a map of Link annotations.
     *
     * @param value
     */
    private Map<String, Link> readLinks(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Link");
        Map<String, Link> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readLink(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Link annotation into a model.
     *
     * @param annotation
     */
    private Link readLink(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Link");
        Link link = new LinkImpl();
        link.setOperationRef(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_OPERATION_REF));
        link.setOperationId(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_OPERATION_ID));
        link.setParameters(readLinkParameters(annotation.value(OpenApiConstants.PROP_PARAMETERS)));
        link.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        link.setRequestBody(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_REQUEST_BODY));
        link.setServer(readServer(annotation.value(OpenApiConstants.PROP_SERVER)));
        link.setRef(JandexUtil.refValue(annotation, RefType.Link));
        return link;
    }

    /**
     * Reads an array of LinkParameter annotations into a map.
     *
     * @param value
     */
    private Map<String, Object> readLinkParameters(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        AnnotationInstance[] nestedArray = value.asNestedArray();
        Map<String, Object> linkParams = new LinkedHashMap<>();
        for (AnnotationInstance annotation : nestedArray) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name != null) {
                String expression = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXPRESSION);
                linkParams.put(name, expression);
            }
        }
        return linkParams;
    }

    /**
     * Reads a map of Parameter annotations.
     *
     * @param value
     */
    private Map<String, Parameter> readParameters(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Parameter");
        Map<String, Parameter> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                Parameter parameter = readParameter(nested);
                if (parameter != null) {
                    map.put(name, parameter);
                }
            }
        }
        return map;
    }

    /**
     * Reads a Parameter annotation into a model.
     *
     * @param annotation
     */
    private Parameter readParameter(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Link");

        // Params can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_HIDDEN);
        if (isHidden != null && isHidden == Boolean.TRUE) {
            return null;
        }

        Parameter parameter = new ParameterImpl();
        parameter.setName(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME));
        parameter.setIn(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_IN, In.class));
        parameter.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        parameter.setRequired(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_REQUIRED));
        parameter.setDeprecated(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_DEPRECATED));
        parameter.setAllowEmptyValue(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_STYLE, Parameter.Style.class));
        parameter.setExplode(readExplode(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_EXPLODE, Explode.class)));
        parameter.setAllowReserved(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_RESERVED));
        parameter.setSchema(readSchema(annotation.value(OpenApiConstants.PROP_SCHEMA)));
        parameter.setContent(readContent(annotation.value(OpenApiConstants.PROP_CONTENT), ContentDirection.Parameter));
        parameter.setExamples(readExamples(annotation.value(OpenApiConstants.PROP_EXAMPLES)));
        parameter.setExample(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXAMPLE));
        parameter.setRef(JandexUtil.refValue(annotation, RefType.Parameter));
        return parameter;
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     *
     * @param enumValue
     */
    private Boolean readExplode(Explode enumValue) {
        if (enumValue == Explode.TRUE) {
            return Boolean.TRUE;
        }
        if (enumValue == Explode.FALSE) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * Reads a single Content annotation into a model.  The value in this case is an array of
     * Content annotations.
     *
     * @param value
     */
    private Content readContent(AnnotationValue value, ContentDirection direction) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Content");
        Content content = new ContentImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String contentType = JandexUtil.stringValue(nested, OpenApiConstants.PROP_MEDIA_TYPE);
            MediaType mediaTypeModel = readMediaType(nested);
            if (contentType == null) {
                // If the content type is not provided in the @Content annotation, then
                // we assume it applies to all the jax-rs method's @Consumes or @Produces
                String[] mimeTypes = {};
                if (direction == ContentDirection.Input && currentConsumes != null) {
                    mimeTypes = currentConsumes;
                }
                if (direction == ContentDirection.Output && currentProduces != null) {
                    mimeTypes = currentProduces;
                }
                if (direction == ContentDirection.Parameter) {
                    mimeTypes = OpenApiConstants.DEFAULT_PARAMETER_MEDIA_TYPES;
                }
                for (String mimeType : mimeTypes) {
                    content.addMediaType(mimeType, mediaTypeModel);
                }
            } else {
                content.addMediaType(contentType, mediaTypeModel);
            }
        }
        return content;
    }

    /**
     * Reads a single Content annotation into a {@link MediaType} model.
     */
    private MediaType readMediaType(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Content (as MediaType)");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setExamples(readExamples(annotation.value(OpenApiConstants.PROP_EXAMPLES)));
        mediaType.setSchema(readSchema(annotation.value(OpenApiConstants.PROP_SCHEMA)));
        mediaType.setEncoding(readEncodings(annotation.value(OpenApiConstants.PROP_ENCODING)));
        return mediaType;
    }

    /**
     * Reads an array of Encoding annotations as a Map.
     *
     * @param value
     */
    private Map<String, Encoding> readEncodings(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Encoding");
        Map<String, Encoding> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readEncoding(annotation));
            }
        }
        return map;
    }

    /**
     * Reads a single Encoding annotation into a model.
     *
     * @param annotation
     */
    private Encoding readEncoding(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Encoding");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_CONTENT_TYPE));
        encoding.setStyle(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_STYLE, Encoding.Style.class));
        encoding.setExplode(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_EXPLODE));
        encoding.setAllowReserved(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_RESERVED));
        encoding.setHeaders(readHeaders(annotation.value(OpenApiConstants.PROP_HEADERS)));
        return encoding;
    }

    /**
     * Reads a map of RequestBody annotations.
     *
     * @param value
     */
    private Map<String, RequestBody> readRequestBodies(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@RequestBody");
        Map<String, RequestBody> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readRequestBody(nested));
            }
        }
        return map;
    }

    /**
     * Reads a RequestBody annotation into a model.
     *
     * @param value
     */
    private RequestBody readRequestBody(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readRequestBody(value.asNested());
    }

    /**
     * Reads a RequestBody annotation into a model.
     *
     * @param annotation
     */
    private RequestBody readRequestBody(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@RequestBody");
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        requestBody.setContent(readContent(annotation.value(OpenApiConstants.PROP_CONTENT), ContentDirection.Input));
        requestBody.setRequired(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_REQUIRED));
        requestBody.setRef(JandexUtil.refValue(annotation, RefType.RequestBody));
        return requestBody;
    }

    /**
     * Reads a map of APIResponse annotations.
     *
     * @param value
     */
    private Map<String, APIResponse> readResponses(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@APIResponse");
        Map<String, APIResponse> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readResponse(nested));
            }
        }
        return map;
    }

    /**
     * Reads a APIResponse annotation into a model.
     *
     * @param annotation
     */
    private APIResponse readResponse(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Response");
        APIResponse response = new APIResponseImpl();
        response.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        response.setHeaders(readHeaders(annotation.value(OpenApiConstants.PROP_HEADERS)));
        response.setLinks(readLinks(annotation.value(OpenApiConstants.PROP_LINKS)));
        response.setContent(readContent(annotation.value(OpenApiConstants.PROP_CONTENT), ContentDirection.Output));
        response.setRef(JandexUtil.refValue(annotation, RefType.Response));
        return response;
    }

    /**
     * Reads a map of Schema annotations.
     *
     * @param value
     */
    private Map<String, Schema> readSchemas(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@Schema");
        Map<String, Schema> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readSchema(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Schema annotation into a model.
     */
    private Schema readSchema(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readSchema(value.asNested());
    }

    /**
     * Reads a Schema annotation into a model.
     *
     * @param annotation
     */
    @SuppressWarnings("unchecked")
    private Schema readSchema(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@Schema");

        // Schemas can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_HIDDEN);
        if (isHidden != null && isHidden == Boolean.TRUE) {
            return null;
        }

        Schema schema = new SchemaImpl();

        schema.setNot(readClassSchema(annotation.value(OpenApiConstants.PROP_NOT), true));
        schema.setOneOf(readClassSchemas(annotation.value(OpenApiConstants.PROP_ONE_OF)));
        schema.setAnyOf(readClassSchemas(annotation.value(OpenApiConstants.PROP_ANY_OF)));
        schema.setAllOf(readClassSchemas(annotation.value(OpenApiConstants.PROP_ALL_OF)));
        schema.setTitle(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_TITLE));
        schema.setMultipleOf(JandexUtil.bigDecimalValue(annotation, OpenApiConstants.PROP_MULTIPLE_OF));
        schema.setMaximum(JandexUtil.bigDecimalValue(annotation, OpenApiConstants.PROP_MAXIMUM));
        schema.setExclusiveMaximum(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_EXCLUSIVE_MAXIMUM));
        schema.setMinimum(JandexUtil.bigDecimalValue(annotation, OpenApiConstants.PROP_MINIMUM));
        schema.setExclusiveMinimum(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_EXCLUSIVE_MINIMUM));
        schema.setMaxLength(JandexUtil.intValue(annotation, OpenApiConstants.PROP_MAX_LENGTH));
        schema.setMinLength(JandexUtil.intValue(annotation, OpenApiConstants.PROP_MIN_LENGTH));
        schema.setPattern(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_PATTERN));
        schema.setMaxProperties(JandexUtil.intValue(annotation, OpenApiConstants.PROP_MAX_PROPERTIES));
        schema.setMinProperties(JandexUtil.intValue(annotation, OpenApiConstants.PROP_MIN_PROPERTIES));
        schema.setRequired(JandexUtil.stringListValue(annotation, OpenApiConstants.PROP_REQUIRED_PROPERTIES));
        schema.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        schema.setFormat(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_FORMAT));
        schema.setRef(JandexUtil.refValue(annotation, RefType.Schema));
        schema.setNullable(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_NULLABLE));
        schema.setReadOnly(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_READ_ONLY));
        schema.setWriteOnly(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_WRITE_ONLY));
        schema.setExample(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXAMPLE));
        schema.setExternalDocs(readExternalDocs(annotation.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        schema.setDeprecated(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_DEPRECATED));
        schema.setType(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_TYPE, Schema.SchemaType.class));
        schema.setEnumeration((List<Object>) (Object) JandexUtil.stringListValue(annotation, OpenApiConstants.PROP_ENUMERATION));
        schema.setDefaultValue(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DEFAULT_VALUE));
        schema.setDiscriminator(readDiscriminatorMappings(annotation.value(OpenApiConstants.PROP_DISCRIMINATOR_MAPPING)));
        schema.setMaxItems(JandexUtil.intValue(annotation, OpenApiConstants.PROP_MAX_ITEMS));
        schema.setMinItems(JandexUtil.intValue(annotation, OpenApiConstants.PROP_MIN_ITEMS));
        schema.setUniqueItems(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_UNIQUE_ITEMS));

        if (JandexUtil.isSimpleClassSchema(annotation)) {
            Schema implSchema = readClassSchema(annotation.value(OpenApiConstants.PROP_IMPLEMENTATION), true);
            schema = MergeUtil.mergeObjects(implSchema, schema);
        } else if (JandexUtil.isSimpleArraySchema(annotation)) {
            Schema implSchema = readClassSchema(annotation.value(OpenApiConstants.PROP_IMPLEMENTATION), true);
            // If the @Schema annotation indicates an array type, then use the Schema
            // generated from the implementation Class as the "items" for the array.
            schema.setItems(implSchema);
        } else {
            Schema implSchema = readClassSchema(annotation.value(OpenApiConstants.PROP_IMPLEMENTATION), false);
            // If there is an impl class - merge the @Schema properties *onto* the schema
            // generated from the Class so that the annotation properties override the class
            // properties (as required by the MP+OAI spec).
            schema = MergeUtil.mergeObjects(implSchema, schema);
        }

        return schema;
    }

    /**
     * Reads an array of Class annotations to produce a list of {@link Schema} models.
     *
     * @param value
     */
    private List<Schema> readClassSchemas(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("schema Class");
        Type[] classArray = value.asClassArray();
        List<Schema> schemas = new ArrayList<>(classArray.length);
        for (Type type : classArray) {
            ClassType ctype = (ClassType) type;
            Schema schema = introspectClassToSchema(ctype, true);
            schemas.add(schema);
        }
        return schemas;
    }

    /**
     * Introspect into the given Class to generate a Schema model.
     *
     * @param value
     */
    private Schema readClassSchema(AnnotationValue value, boolean schemaReferenceSupported) {
        if (value == null) {
            return null;
        }
        ClassType ctype = (ClassType) value.asClass();
        Schema schema = introspectClassToSchema(ctype, schemaReferenceSupported);
        return schema;
    }

    /**
     * Introspects the given class type to generate a Schema model.  The boolean indicates
     * whether this class type should be turned into a reference.
     *
     * @param ctype
     * @param schemaReferenceSupported
     */
    private Schema introspectClassToSchema(ClassType ctype, boolean schemaReferenceSupported) {
        if (ctype.name().equals(OpenApiConstants.DOTNAME_RESPONSE)) {
            return null;
        }
        if (schemaReferenceSupported && this.schemaRegistry.has(ctype)) {
            GeneratedSchemaInfo schemaInfo = this.schemaRegistry.lookup(ctype);
            Schema rval = new SchemaImpl();
            rval.setRef(schemaInfo.$ref);
            return rval;
        } else {
            Schema schema = OpenApiDataObjectScanner.process(index, ctype);
            if (schemaReferenceSupported && schema != null && this.index.getClassByName(ctype.name()) != null) {
                GeneratedSchemaInfo schemaInfo = this.schemaRegistry.register(ctype, schema);
                ModelUtil.components(oai).addSchema(schemaInfo.name, schema);
                Schema rval = new SchemaImpl();
                rval.setRef(schemaInfo.$ref);
                return rval;
            } else {
                return schema;
            }
        }
    }

    /**
     * Reads an array of DiscriminatorMapping annotations into a {@link Discriminator} model.
     *
     * @param value
     */
    private Discriminator readDiscriminatorMappings(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@DiscriminatorMapping");
        Discriminator discriminator = new DiscriminatorImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (@SuppressWarnings("unused") AnnotationInstance nested : nestedArray) {
            // TODO iterate the discriminator mappings and do something sensible with them! :(
        }
        return discriminator;
    }

    /**
     * Reads a map of SecurityScheme annotations.
     *
     * @param value
     */
    private Map<String, SecurityScheme> readSecuritySchemes(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@SecurityScheme");
        Map<String, SecurityScheme> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readSecurityScheme(nested));
            }
        }
        return map;
    }

    /**
     * Reads a SecurityScheme annotation into a model.
     *
     * @param annotation
     */
    private SecurityScheme readSecurityScheme(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@SecurityScheme");
        SecurityScheme securityScheme = new SecuritySchemeImpl();
        securityScheme.setType(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_TYPE, SecurityScheme.Type.class));
        securityScheme.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        securityScheme.setName(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_API_KEY_NAME));
        securityScheme.setIn(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_IN, SecurityScheme.In.class));
        securityScheme.setScheme(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SCHEME));
        securityScheme.setBearerFormat(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_BEARER_FORMAT));
        securityScheme.setFlows(readOAuthFlows(annotation.value(OpenApiConstants.PROP_FLOWS)));
        securityScheme.setOpenIdConnectUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(JandexUtil.refValue(annotation, RefType.SecurityScheme));
        return securityScheme;
    }

    /**
     * Reads an OAuthFlows annotation into a model.
     *
     * @param value
     */
    private OAuthFlows readOAuthFlows(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@OAuthFlows");
        AnnotationInstance annotation = value.asNested();
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(readOAuthFlow(annotation.value(OpenApiConstants.PROP_IMPLICIT)));
        flows.setPassword(readOAuthFlow(annotation.value(OpenApiConstants.PROP_PASSWORD)));
        flows.setClientCredentials(readOAuthFlow(annotation.value(OpenApiConstants.PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(readOAuthFlow(annotation.value(OpenApiConstants.PROP_AUTHORIZATION_CODE)));
        return flows;
    }

    /**
     * Reads a single OAuthFlow annotation into a model.
     *
     * @param value
     */
    private OAuthFlow readOAuthFlow(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingAnnotation("@OAuthFlow");
        AnnotationInstance annotation = value.asNested();
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_TOKEN_URL));
        flow.setRefreshUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_REFRESH_URL));
        flow.setScopes(readOAuthScopes(annotation.value(OpenApiConstants.PROP_SCOPES)));
        return flow;
    }

    /**
     * Reads an array of OAuthScope annotations into a Scopes model.
     *
     * @param value
     */
    private Scopes readOAuthScopes(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        OpenApiMessages.MESSAGES.processingArrayOfAnnotation("@OAuthScope");
        AnnotationInstance[] nestedArray = value.asNestedArray();
        Scopes scopes = new ScopesImpl();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name != null) {
                String description = JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION);
                scopes.addScope(name, description);
            }
        }
        return scopes;
    }

    /**
     * Reads an array of Extension annotations.  The AnnotationValue in this case is
     * an array of Extension annotations.  These must be read and converted into a Map.
     *
     * @param value
     */
    private Map<String, Object> readExtensions(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> extensions = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String extName = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            String extValue = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_VALUE);
            extensions.put(extName, extValue);
        }
        return extensions;
    }

    /**
     * Simple enum to indicate whether an @Content annotation being processed is
     * an input or an output.
     *
     * @author eric.wittmann@gmail.com
     */
    private enum ContentDirection {
        Input, Output, Parameter
    }

    /**
     * Information about a single generated schema.
     *
     * @author eric.wittmann@gmail.com
     */
    protected static class GeneratedSchemaInfo {
        public String name;

        public Schema schema;

        public String $ref;
    }

    /**
     * A simple registry used to track schemas that have been generated and inserted
     * into the #/components section of the
     *
     * @author eric.wittmann@gmail.com
     */
    protected static class SchemaRegistry {
        private Map<DotName, GeneratedSchemaInfo> registry = new HashMap<>();

        private Set<String> names = new HashSet<>();

        GeneratedSchemaInfo register(ClassType instanceClass, Schema schema) {
            String name = instanceClass.name().local();
            int idx = 1;
            while (this.names.contains(name)) {
                name = instanceClass.name().local() + idx++;
            }
            GeneratedSchemaInfo info = new GeneratedSchemaInfo();
            info.schema = schema;
            info.name = name;
            info.$ref = "#/components/schemas/" + name;

            registry.put(instanceClass.name(), info);
            names.add(name);

            return info;
        }

        GeneratedSchemaInfo lookup(ClassType instanceClass) {
            return registry.get(instanceClass.name());
        }

        boolean has(ClassType instanceClass) {
            return registry.containsKey(instanceClass.name());
        }
    }

}
