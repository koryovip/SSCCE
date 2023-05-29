package jp.co.syslinks.sscce.java.java8;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.github.javaparser.printer.PrettyPrinterConfiguration.IndentType;

public class JavaParserTest {

    public static void main(String[] args) {
        JavaParser.getStaticConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8);
        PrettyPrinterConfiguration conf = new PrettyPrinterConfiguration();
        conf.setIndentType(IndentType.TABS);
        conf.setIndentSize(1);
        conf.setPrintComments(false);
        conf.setPrintJavadoc(false);
        conf.setEndOfLineCharacter("\n");
        File root = new File("dir");
        dddd(root, conf);
    }

    private static void dddd(File root, PrettyPrinterConfiguration conf) {
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                dddd(file, conf);
            }
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    System.out.println(file);
                    try {
                        parse(file.toPath(), conf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void parse(Path path, PrettyPrinterConfiguration conf) throws IOException {
        CompilationUnit unit = JavaParser.parse(path);
        String source = unit.toString(conf);
        try (PrintWriter pw = new PrintWriter(path.toFile(), StandardCharsets.UTF_8.name())) {
            pw.write(source);
            pw.flush();
        }

    }

}
