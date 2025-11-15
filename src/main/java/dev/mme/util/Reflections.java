package dev.mme.util;

import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Reflections {
    private final String packageName;
    public static final Reflections DEFAULT = new Reflections("dev.mme");

    public Reflections(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Finds and casts all static instances of the subclasses provided to the abstract class or interface T
     * @param subclasses The Class objects of the subclasses
     * @return List of instances provided by subclasses that has a {@code static INSTANCE} field or a {@code static getInstance} method
     * @param <T> The interface or abstract super class to cast to
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getInstances(final Iterable<Class<? extends T>> subclasses) {
        final List<T> instances = new ArrayList<>();
        for (Class<? extends T> clazz : subclasses) {
            try {
                T instance;
                try {
                    Field instanceField = clazz.getDeclaredField("INSTANCE");
                    instanceField.setAccessible(true);
                    instance = (T) instanceField.get(null);
                } catch (NoSuchFieldException | NullPointerException ex) {
                    Method getInstance = clazz.getDeclaredMethod("getInstance");
                    getInstance.setAccessible(true);
                    instance = (T) getInstance.invoke(null);
                }
                instances.add(instance);
            } catch (Exception ignored) {}
        }
        return instances;
    }

    /**
     * Finds and loads all concrete, referencable subclasses of the abstract class or interface T
     * @param type The Class object of the interface or abstract super
     * @return Set of concrete, referencable subclasses of the abstract class or interface T
     * @param <T> The interface or abstract super
     */
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        Set<Class<? extends T>> result = new HashSet<>();
        try {
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
                    Path pth = new File(filePath).toPath();
                    if (Files.isDirectory(pth)) {
                        try {
                            Stream<Path> entries = Files.walk(pth)
                                    .filter(Files::isRegularFile)
                                    .filter(file -> file.toString().endsWith(".class"));
                            entries.forEach(file -> {
                                String relativePath = pth.relativize(file).toString();
                                String className = packageName + '.' + relativePath.substring(0, relativePath.length() - 6).replace(File.separatorChar, '.');
                                addClassIfSubtype(type, result, className);
                            });
                            entries.close();
                        } catch (IOException ignored) {}
                    }
                } else if ("jar".equals(protocol)) {
                    JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
                    JarFile jarFile = jarConn.getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                            String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                            addClassIfSubtype(type, result, className);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Utils.logError(ex, "Caught IOException while scanning subtypes");
        }
        return result;
    }

    /**
     * Tests and adds to result if the Class object represented by className is a concrete, referencable subclass of the abstract class or interface T
     * @param type The Class object of the interface or abstract super
     * @param result The result set
     * @param className The class name of the class to test
     * @param <T> Interface or abstract super
     */
    private <T> void addClassIfSubtype(Class<T> type, Set<Class<? extends T>> result, String className) {
        try {
            ClassInfo info = ClassInfo.forName(className);
            if (info == null || info.isMixin() || info.isAbstract()) return;

            String targetName = type.getName().replace('.', '/');

            boolean matches = info.getInterfaces().contains(targetName) || info.hasSuperClass(targetName);

            if (!matches) {
                ClassInfo superInfo = info.getSuperClass();
                while (superInfo != null) {
                    if (superInfo.getName().equals(targetName)
                            || superInfo.getInterfaces().contains(targetName)
                            || superInfo.hasSuperClass(targetName)) {
                        matches = true;
                        break;
                    }
                    superInfo = superInfo.getSuperClass();
                }
            }

            if (matches) {
                result.add(Class.forName(className).asSubclass(type));
            }
        } catch (ClassNotFoundException ex) {
           Utils.logError(ex, "Subtype scanner should not have reached this point");
        } catch (NullPointerException ex) {
            Utils.logError(ex, "Subtype scanner received a faulty className");
        }
    }
}
