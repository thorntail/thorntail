package io.thorntail.plugins.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by bob on 2/13/18.
 */
public class MainFinder extends ClassVisitor {

    private static final String MAIN = "main";

    private static final String DESCRIPTOR = "([Ljava/lang/String;)V";

    public MainFinder(File archive) {
        super(Opcodes.ASM6);
        this.archive = archive;
    }

    public List<String> search() throws IOException {
        try (JarFile jar = new JarFile(this.archive)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry each = entries.nextElement();
                search(jar, each);
            }
        }

        return this.mainClasses;
    }

    void search(JarFile jar, JarEntry entry) throws IOException {
        if (!entry.getName().endsWith(".class")) {
            return;
        }
        try (InputStream in = jar.getInputStream(entry)) {
            ClassReader reader = new ClassReader(in);
            reader.accept(this, 0);
        }
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if ((Opcodes.ACC_PUBLIC & access) == 0) {
            return;
        }
        this.currentClass = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals(MAIN)) {
            if (((Opcodes.ACC_STATIC & access) != 0) && (Opcodes.ACC_PUBLIC & access) != 0) {
                if (descriptor.equals(DESCRIPTOR)) {
                    String className = this.currentClass.replace('/', '.');
                    this.mainClasses.add(className);
                }
            }
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    private String currentClass;

    private final File archive;

    private final List<String> mainClasses = new ArrayList<>();
}
