package org.wildfly.swarm.integration.staticcontent;

public interface StaticContentCommonTests {

    default void assertBasicStaticContentWorks(String context) throws Exception {
        if (context.length() > 0 && !context.endsWith("/")) {
            context = context + "/";
        }
        assertContains(context + "static-content.txt", "This is static.");
        assertContains(context + "index.html", "This is index.html.");
        assertContains(context + "foo/index.html", "This is foo/index.html.");
        // Ensure index files are used
        assertContains(context, "This is index.html.");
        assertContains(context + "foo", "This is foo/index.html.");
        // Ensure content under src/main/resources is NOT served up
        assertNotFound(context + "faildex.html");
        // Ensure we don't serve up Java class files
        assertNotFound(context + "java/lang/Object.class");
        // And doubly ensure we don't serve up application class files
        assertNotFound(context + this.getClass().getName().replace(".", "/") + ".class");
    }

    void assertContains(String path, String content) throws Exception;

    void assertNotFound(String path) throws Exception;
}
