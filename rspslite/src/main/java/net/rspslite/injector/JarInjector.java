package net.rspslite.injector;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import javassist.CtClass;
import javassist.ClassPool;
import javassist.ByteArrayClassPath;

public class JarInjector {

  public static void inject(String inputJarPath, String outputJarPath, Map<String, Consumer<CtClass>> classInjectors) throws IOException {
    ClassPool cp = readClassPool(inputJarPath);
    transform(cp, classInjectors);
    outputJar(inputJarPath, cp, outputJarPath);
  }

  private static ClassPool readClassPool(String inputJarPath) throws IOException {
    ClassPool cp = ClassPool.getDefault();

    mapEntries(inputJarPath, (name, data) -> {
      if (name.endsWith(".class")) {
        String className = getClassName(name);
        cp.appendClassPath(new ByteArrayClassPath(className, data));
      }
    });

    return cp;
  }

  private static void transform(ClassPool cp, Map<String, Consumer<CtClass>> classInjectors) {
    for (String className : classInjectors.keySet()) {
      Consumer<CtClass> transformer = classInjectors.get(className);
      try {
        CtClass cc = cp.get(className);
        transformer.accept(cc);
      } catch (javassist.NotFoundException e) {
        System.err.println("Unable to apply injector because could not find " + className);
      }
    }
  }

  private static void outputJar(String inputJarPath, ClassPool cp, String outputJarPath) throws IOException {
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputJarPath));
    mapEntries(inputJarPath, (name, data) -> {
      if (name.endsWith(".class")) {
        String className = getClassName(name);
        try {
          CtClass cc = cp.get(className);
          data = cc.toBytecode();
        } catch (javassist.NotFoundException e) {
          System.err.println("Unable to use injected class because could not find " + className + ". Defaulting to non-injected class.");
        } catch (javassist.CannotCompileException e) {
          System.err.println("Unable to use injected class " + className + " because could not compile. Defaulting to non-injected class.");
        } catch (IOException e) {
          System.err.println("Unable to use injected class " + className + " because IOException when converting to bytecode. Defaulting to non-injected class.");
        }
      }
      try {
        out.putNextEntry(new ZipEntry(name));
        out.write(data);
        out.closeEntry();
      } catch (IOException e) {
        System.err.println("Unable to write " + name + " to JAR. Skipping.");
      }
    });
    out.close();
  }

  private static void mapEntries(String inputJarPath, BiConsumer<String, byte[]> f) throws IOException {
    ZipInputStream in = new ZipInputStream(new FileInputStream(inputJarPath));

    ZipEntry entry;
    while ((entry = in.getNextEntry()) != null) {
      byte[] data = readEntry(in, entry);
      f.accept(entry.getName(), data);
    }

    in.close();
  }

  private static byte[] readEntry(ZipInputStream in, ZipEntry entry) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;
    while ((len = in.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
    return out.toByteArray();
  }

  private static String getClassName(String fileName) {
    return fileName.replace("/", ".").substring(0, fileName.length() - ".class".length());
  }

}
