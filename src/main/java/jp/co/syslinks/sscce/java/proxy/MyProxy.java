package jp.co.syslinks.sscce.java.proxy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * 该类可以理解为方法类 其作用就是为了动态构建 代理类并实例化 的一个入口
 * @author ngcly
 * @version V1.0
 * @since 2021/8/20 15:03
 */
public class MyProxy {
    //自增数字
    private static final AtomicLong ATOMIC_LONG = new AtomicLong();

    //私有化构造 因为该类作用就是个方法入口
    private MyProxy() {
    }

    /** 以字符串的形式拼凑出一个 实现了 interfaces 接口的代理类  同时该类还需要有 MyInvocationHandler 成员变量，
     * MyInvocationHandler 该接口 就是执行代理的操作的接口
     将该类的构造方法改为要传入handle的有参构造 然后该代理类实现的所有方法实现为 调用 handler接口的 invoke方法
     将上述字符串类编译成class类， 然后利用ClassLoader进行加载
     最后用反射将该class进行实例化 并返回 */
    public static Object newProxyInstance(MyClassLoader classLoader, Class<?>[] interfaces, MyInvocationHandler handler) {

        String className = "$Proxy" + ATOMIC_LONG.getAndIncrement();
        try {
            String javaFileName = MyProxy.class.getResource("").getPath() + className + ".java";

            // 以下 2，3，4 步骤目的就是构建一个代理类的class文件   JDK此处就是直接构造成 class 字节码然后保存到文件中。
            // 由于Class文件结构较为复杂 所以此处是先用字符串拼接一个Java文件，
            // 再用JavaCompiler去编译这个java文件，生成一个class文件。
            // 最后通过自定义的MyClassLoader类加载器把这个生成好的class文件加载到内存，得到Class对象。

            // 1.生成源代码字符串
            String javaSrc = generateJavaSrc(className, interfaces);

            // 2.把源代码字符串作为java类文件保存到磁盘
            File javaFile = new File(javaFileName);
            generateJavaFile(javaSrc, javaFile);

            // 3.将磁盘的代理Java类编译成class文件
            compilerJavaFile(javaFile);

            // 4.使用自定义的 classLoad 将刚才编译的代理class加载到jvm中
            Class<?> proxyClass = classLoader.loadClass(className);

            // 5.利用反射 将代理class实例化 并返回该代理对象
            Object obj = proxyClass.getDeclaredConstructor(MyInvocationHandler.class).newInstance(handler);
            if (javaFile.exists()) {
                //删除操作时生成的Java源代码文件
                javaFile.delete();
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 构建Java源码字符串
     * @param className java类名
     * @param interfaces 要继承或实现的类或接口
     * @return String 源码字符串
     */
    public static String generateJavaSrc(String className, Class<?>[] interfaces) {
        // 定义换行符
        String ln = "\r\n";
        String packageName = MyProxy.class.getPackage().getName();
        StringBuffer sb = new StringBuffer();
        //类所在包
        sb.append("package " + packageName + ";" + ln);
        //导包代码
        sb.append("import java.lang.reflect.Method;" + ln);
        //类定义代码
        sb.append("public class " + className);
        //此处判断若是普通类则用继承
        if (!interfaces[0].isInterface()) {
            sb.append(" extends ").append(interfaces[0].getName());
        } else {
            sb.append(" implements ");
            sb.append(Arrays.stream(interfaces).map(Class::getName).collect(Collectors.joining(",")));
        }
        sb.append("{" + ln);
        //属性
        sb.append("MyInvocationHandler handler;" + ln);
        // 构造方法
        sb.append("public " + className + "(MyInvocationHandler handler){" + ln);
        sb.append("this.handler = handler;" + ln);
        sb.append("}").append(ln);

        //拼接代理类的方法
        for (Class<?> intface : interfaces) {
            for (Method method : intface.getDeclaredMethods()) {
                //方法参数列表
                Class<?>[] parameterTypes = method.getParameterTypes();
                //用于拼接形参列表
                StringBuffer formalArgs = new StringBuffer();
                //用于拼接实参列表
                StringBuffer realArgs = new StringBuffer();
                //用于拼接实参Class类型的字符串
                StringBuffer realArgsClass = new StringBuffer();
                Iterator<Class<?>> iterator = Arrays.stream(parameterTypes).iterator();
                while (iterator.hasNext()) {
                    Class<?> parameterType = iterator.next();
                    String argName = "arg" + ATOMIC_LONG.getAndIncrement();
                    formalArgs.append(parameterType.getName()).append(" ").append(argName);
                    realArgs.append(argName);
                    realArgsClass.append(parameterType.getName()).append(".class");
                    if (iterator.hasNext()) {
                        formalArgs.append(",");
                        realArgs.append(",");
                        realArgsClass.append(",");
                    }
                }

                //如果参数长度为0，那么传null
                String arg = parameterTypes.length > 0 ? "new Object[]{" + realArgs + "}" : "null";

                //拼接成员方法体代码
                sb.append("    @Override");
                sb.append("    public ").append(method.getReturnType().getName()).append(" ").append(method.getName()).append("(").append(formalArgs).append("){").append(ln);
                sb.append("        try{ " + ln);
                sb.append("            Method method = " + intface.getName() + ".class.getMethod(\"" + method.getName() + "\"");
                //根据参数长度，确定是否添加参数
                if (parameterTypes.length > 0) {
                    sb.append("," + realArgsClass);
                }
                sb.append(");" + ln + "            ");
                //返回值 如果为"void"，则生成没有返回值的方法调用
                if (!method.getReturnType().getName().equals("void")) {
                    sb.append("return (" + method.getReturnType().getName() + ")");
                }
                sb.append("handler.invoke(this,method," + arg + ");" + ln);

                //捕获所有异常，转换成RuntimeException异常
                sb.append("        }catch (Throwable e){e.printStackTrace();throw new RuntimeException(e.getMessage());}" + ln);
                sb.append("}" + ln);
            }
        }
        sb.append("}" + ln);

        return sb.toString();
    }

    /**
     * 在磁盘生成.java文件
     * @param javaSrc java源码字符串
     * @param javaFile java文件
     */
    public static void generateJavaFile(String javaSrc, File javaFile) throws IOException {
        try (FileWriter fw = new FileWriter(javaFile)) {
            fw.write(javaSrc);
        }
    }

    /**
     * 编译.java文件
     */
    public static void compilerJavaFile(File javaFile) {
        //运行时编译器  编译代理类的Java源代码代码->class文件
        System.out.println("------------------------------------->compilerJavaFile");
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> iterable = fileManager.getJavaFileObjects(javaFile);
            JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, null, null, null, iterable);
            task.call();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}