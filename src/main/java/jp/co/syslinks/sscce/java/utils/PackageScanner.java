package jp.co.syslinks.sscce.java.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class PackageScanner {

    public abstract void dealClass(Class<?> clazz);

    private final String dotClass = ".class";

    private void scanPackage(String packageName, File currentFile) {
        // File[] fileList;
        for (File file : currentFile.listFiles(pathName -> {
            if (pathName.isDirectory()) {
                return true;
            }
            return pathName.getName().endsWith(dotClass);
        })) {
            if (file.isDirectory()) {
                this.scanPackage(packageName + "." + file.getName(), file);
                continue;
            }
            String fileName = file.getName().replace(dotClass, "");
            String className = packageName + "." + fileName;
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotation() || clazz.isEnum() || clazz.isPrimitive() /*|| clazz.isInterface()*/) {
                    continue;
                }
                this.dealClass(clazz);
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void scanPackage(URL url) throws IOException {
        JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
        JarFile jarfile = urlConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarfile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarName = jarEntry.getName();
            if (!jarName.endsWith(dotClass)) {
                continue;
            }
            String className = jarName.replace(dotClass, "").replaceAll("/", ".");
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotation() || clazz.isEnum() || clazz.isPrimitive() /*|| clazz.isInterface()*/) {
                    continue;
                }
                this.dealClass(clazz);
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void packageScan(Class<?> clazz) {
        this.packageScan(clazz.getPackage().getName());
    }

    public void packageScan(String packageName) {
        String packOpperPath = packageName.replace(".", "/");
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classloader.getResources(packOpperPath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar")) {
                    this.scanPackage(url);
                    continue;
                }
                File file = new File(url.toURI());
                if (!file.exists()) {
                    continue;
                }
                this.scanPackage(packageName, file);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PackageScanner scanner = new PackageScanner() {
            @Override
            public void dealClass(Class<?> clazz) {
                System.out.println(clazz);
            }
        };
        scanner.packageScan("jp.co.syslinks");
    }

}