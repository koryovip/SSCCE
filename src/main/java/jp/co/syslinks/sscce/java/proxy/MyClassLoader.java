package jp.co.syslinks.sscce.java.proxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * 自定义类加载器
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 17:42
 */
public class MyClassLoader extends ClassLoader {
    private File baseDir;

    public MyClassLoader() {
        String basePath = MyClassLoader.class.getResource("").getPath();
        this.baseDir = new File(basePath);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String className = MyClassLoader.class.getPackage().getName() + "." + name;
        File file = new File(this.baseDir, name.replace(".", "//") + ".class");
        System.out.println(file);
        byte[] buff = new byte[1024];
        int len = -1;
        try (FileInputStream in = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // in.transferTo(out);
            while ((len = in.read(buff, 0, 1024)) != -1) {
                out.write(buff, 0, len);
            }
            return defineClass(className, out.toByteArray(), 0, out.size());
        } catch (Exception e) {
            throw new ClassNotFoundException();
        }
    }

}