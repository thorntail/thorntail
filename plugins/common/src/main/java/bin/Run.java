package bin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Created by bob on 2/12/18.
 */
public class Run {

    public static final Attributes.Name MAIN_CLASS_ATTRIBUTE_NAME = new Attributes.Name("thorntail-main-class");

    private static final String MANIFEST = "META-INF/MANIFEST.MF";

    private static final String MAIN = "main";

    public static void main(String... args) throws Exception {
        new Run(args).run();
    }

    Run(String... args) {
        process(args);
        this.cachePath = Paths.get(System.getProperty("user.home")).resolve(".thorntail-cache");
    }

    void run() throws Exception {
        extract();
        runMain();
    }

    private void process(String... args) {
        List<String> myArgs = new ArrayList<>();
        List<String> appArgs = new ArrayList<>();

        boolean mine = true;

        for (String arg : args) {
            if (arg.equals("--")) {
                mine = false;
            } else if (mine) {
                myArgs.add(arg);
            } else {
                appArgs.add(arg);
            }
        }

        this.args = appArgs.toArray(new String[appArgs.size()]);

        for (String myArg : myArgs) {
            this.classpath.add(Paths.get(myArg));
        }

    }

    private void extract() throws IOException, NoSuchAlgorithmException {
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        if (location.toExternalForm().endsWith(".jar")) {
            try (JarFile jar = new JarFile(new File(location.getPath()))) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry each = entries.nextElement();
                    if (each.getName().endsWith(".jar")) {
                        extract(jar, each);
                    } else if (each.getName().equals(MANIFEST)) {
                        loadManifest(jar, each);
                    }
                }
            }
        }
    }


    private void extract(JarFile jar, JarEntry each) throws IOException, NoSuchAlgorithmException {
        Files.createDirectories(this.cachePath);
        String simpleName = each.getName();
        int slashLoc = simpleName.lastIndexOf("/");
        if (slashLoc > 0) {
            simpleName = simpleName.substring(slashLoc + 1);
        }
        byte[] srcHash = hashOf(jar, each);

        Path dest = this.cachePath.resolve(addHash(simpleName, srcHash));

        if (Files.exists(dest)) {
            this.classpath.add(dest);
            return;
        }
        this.classpath.add(dest);
        try (InputStream in = jar.getInputStream(each)) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String addHash(String simpleName, byte[] hash) {
        String name = simpleName.substring(0, simpleName.length() - ".jar".length());

        name = name + "-" + toString(hash);
        name = name + ".jar";

        return name;
    }

    private String toString(byte[] hash) {
        BigInteger bi = new BigInteger(1, hash);
        return String.format("%0" + (hash.length << 1) + "x", bi).substring(0, 10);
    }

    public static byte[] hashOf(Path path) throws NoSuchAlgorithmException, IOException {
        return hashOf(new FileInputStream(path.toFile()));
    }

    public static byte[] hashOf(JarFile jar, JarEntry each) throws IOException, NoSuchAlgorithmException {
        return hashOf(jar.getInputStream(each));
    }

    public static byte[] hashOf(InputStream stream) throws NoSuchAlgorithmException, IOException {
        byte[] ignored = new byte[4096];
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        try (DigestInputStream in = new DigestInputStream(stream, digest)) {
            while (in.read(ignored) > 0) {
                // loop
            }
        }
        return digest.digest();
    }

    private void loadManifest(JarFile jar, JarEntry each) throws IOException {
        try (InputStream in = jar.getInputStream(each)) {
            this.manifest = new Manifest(in);
        }
    }

    private ClassLoader classLoader() {
        ClassLoader cl = new URLClassLoader(
                this.classpath.stream().map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new)
        );

        return cl;
    }

    private String mainClassName() {
        return this.manifest.getMainAttributes().getValue(MAIN_CLASS_ATTRIBUTE_NAME);
    }

    private Class<?> mainClass() throws ClassNotFoundException {
        return classLoader().loadClass(mainClassName());
    }

    private void runMain() throws Exception {
        Class<?> cls = mainClass();
        Method main = findMain(cls);
        main.invoke(null, new Object[]{this.args});
    }

    private Method findMain(Class<?> cls) throws NoSuchMethodException {
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(MAIN)) {
                if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == 1) {
                        if (params[0] == String[].class) {
                            return method;
                        }
                    }
                }
            }
        }

        throw new NoSuchMethodException("public static void main(String...args)");
    }

    private String[] args;

    private final Path cachePath;

    private final List<Path> classpath = new ArrayList<>();

    private Manifest manifest;
}

