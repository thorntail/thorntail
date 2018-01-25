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

package test.org.wildfly.swarm.microprofile.openapi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.tck.AppTestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.wildfly.microprofile.config.PropertiesConfigSourceProvider;
import org.wildfly.microprofile.config.WildFlyConfigBuilder;
import org.wildfly.swarm.microprofile.openapi.io.OpenApiSerializer;
import org.wildfly.swarm.microprofile.openapi.io.OpenApiSerializer.Format;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConfig;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiDeploymentProcessor;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiDocumentHolder;

/**
 * A Junit 4 test runner used to quickly run the OpenAPI tck tests directly against the
 * {@link OpenApiDeploymentProcessor} without spinning up Wildfly Swarm.  This is not
 * a replacement for running the full OpenAPI TCK using Arquillian.  However, it runs
 * much faster and does *most* of what we need for coverage.
 *
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
public class TckTestRunner extends ParentRunner<ProxiedTckTest> {

    private static final String CLASS_SUFFIX = ".class";

    private Class<?> testClass;
    private Class<? extends AppTestBase> tckTestClass;

    public static Map<Class, OpenAPI> openApiDocs = new HashMap<>();

    /**
     * Constructor.
     * @param testClass
     * @throws InitializationError
     */
    public TckTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        this.tckTestClass = determineTckTestClass(testClass);

        // The Archive (shrinkwrap deployment)
        Archive archive = archive();
        // Index the archive's annotations
        IndexView index = archiveToIndex(archive);
        // MPConfig
        WildFlyConfigBuilder cfgBuilder = new WildFlyConfigBuilder();
        cfgBuilder.addDefaultSources();
        TckTest anno = testClass.getAnnotation(TckTest.class);
        if (anno.configProperties() != null && anno.configProperties().trim().length() > 0) {
            List<ConfigSource> configSources = new PropertiesConfigSourceProvider(anno.configProperties(), true, tckTestClass.getClassLoader()).getConfigSources(tckTestClass.getClassLoader());
            configSources.forEach(source -> {
                cfgBuilder.withSources(source);
            });
        }

        Config mpConfig = cfgBuilder.build();
        OpenApiConfig config = new OpenApiConfig() {
            @Override
            protected Config getConfig() {
                return mpConfig;
            }
        };
        OpenApiDeploymentProcessor processor = new OpenApiDeploymentProcessor(config, archive, index);
        try {
            processor.process();

            Assert.assertNotNull("Generated OAI document must not be null.", OpenApiDocumentHolder.document);

            openApiDocs.put(testClass, OpenApiDocumentHolder.document);

            // Output the /openapi content to a file for debugging purposes
            File parent = new File("target", "TckTestRunner");
            if (!parent.exists()) {
                parent.mkdir();
            }
            File file = new File(parent, testClass.getName() + ".json");
            String content = OpenApiSerializer.serialize(OpenApiDocumentHolder.document, Format.JSON);
            try (FileWriter writer = new FileWriter(file)) {
                IOUtils.write(content, writer);
            }
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    /**
     * Creates and returns the shrinkwrap archive for this test.
     */
    private Archive archive() throws InitializationError {
        try {
            Method[] methods = tckTestClass.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Deployment.class)) {
                    Archive archive = (Archive) method.invoke(null);
                    return archive;
                }
            }
            throw new Exception("No @Deployment archive found for test.");
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    /**
     * @param archive
     * @return
     */
    @SuppressWarnings("unchecked")
    private IndexView archiveToIndex(Archive archive) {
        if (archive == null) {
            return null;
        }

        Indexer indexer = new Indexer();
        Map<ArchivePath, Node> c = archive.getContent();
        try {
            for (Map.Entry<ArchivePath, Node> each : c.entrySet()) {
                if (each.getKey().get().endsWith(CLASS_SUFFIX)) {
                    indexer.index(each.getValue().getAsset().openStream());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return indexer.complete();
    }

    /**
     * Figures out what TCK test is being run.
     * @throws InitializationError
     */
    private Class<? extends AppTestBase> determineTckTestClass(Class<?> testClass) throws InitializationError {
        TckTest anno = testClass.getAnnotation(TckTest.class);
        if (anno == null) {
            throw new InitializationError("Missing annotation @TckTest");
        }
        return anno.test();
    }

    /**
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<ProxiedTckTest> getChildren() {
        List<ProxiedTckTest> children = new ArrayList<>();
        Method[] methods = tckTestClass.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(org.testng.annotations.Test.class)) {
                try {
                    ProxiedTckTest test = new ProxiedTckTest();
                    Object theTestObj = this.testClass.newInstance();
                    test.setTest(theTestObj);
                    test.setTestMethod(method);
                    test.setDelegate(createDelegate(theTestObj));
                    children.add(test);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        children.sort(new Comparator<ProxiedTckTest>() {
            @Override
            public int compare(ProxiedTckTest o1, ProxiedTckTest o2) {
                return o1.getTestMethod().getName().compareTo(o2.getTestMethod().getName());
            }
        });
        return children;
    }

    /**
     * Creates the delegate test instance.  This is done by instantiating the test itself
     * and calling its "getDelegate()" method.  If no such method exists then an error
     * is thrown.
     */
    private AppTestBase createDelegate(Object testObj) throws Exception {
        Object delegate = testObj.getClass().getMethod("getDelegate").invoke(testObj);
        return (AppTestBase) delegate;
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(ProxiedTckTest child) {
        return Description.createTestDescription(tckTestClass, child.getTestMethod().getName());
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final ProxiedTckTest child, final RunNotifier notifier) {
        OpenApiDocumentHolder.document = TckTestRunner.openApiDocs.get(child.getTest().getClass());

        Description description = describeChild(child);
        if (isIgnored(child)) {
            notifier.fireTestIgnored(description);
        } else {
            Statement statement = new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        Object [] args = (Object[]) child.getTest().getClass().getMethod("getTestArguments").invoke(child.getTest());
                        child.getTestMethod().invoke(child.getDelegate(), args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            };
            runLeaf(statement, description, notifier);
        }

    }

    /**
     * @see org.junit.runners.ParentRunner#isIgnored(java.lang.Object)
     */
    @Override
    protected boolean isIgnored(ProxiedTckTest child) {
        return child.getTestMethod().isAnnotationPresent(Ignore.class);
    }

}
