package org.wildfly.swarm.microprofile.config.tck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Disable tests from exclusion list
 * Created by hbraun on 10.01.18.
 */
public class TestNGAnnotationTransformer implements IAnnotationTransformer {

    private Set<String> exclusions = new HashSet();

    public TestNGAnnotationTransformer() {
        try {
            InputStream in = TestNGAnnotationTransformer.class.getClassLoader().getResourceAsStream("TCK-exclusions.txt");
            if(in != null) {
                read(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            buffer.lines().forEach(s -> exclusions.add(s));
        }
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {

        if (testMethod == null || annotation == null) {
            return;
        }

        String clazzName = testMethod.getDeclaringClass().getName();
        exclusions.forEach(s -> {
            if(s.equals(clazzName)) {
                annotation.setEnabled(false);
            }
        });

    }
}
