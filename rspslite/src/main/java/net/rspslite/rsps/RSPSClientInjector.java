package net.rspslite.rsps;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.CannotCompileException;
import net.rspslite.injector.JarInjector;

public class RSPSClientInjector {

  public static Map<String, Consumer<CtClass>> getInjectors(String osrsInjectedClientJarPath, URL[] osrsJars, String rspsClientJarPath, URL[] rspsJars) {

    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    Map<String, String> oldschoolToRSPSClassMap = findClassMap(osrsInjectedClientJarPath, osrsJars, rspsClientJarPath, rspsJars);

    /*injectors.put("Alora", (cc) -> {
      try {

        CtConstructor constructor = CtNewConstructor.make("public Alora() { this(\"1\", true); }", cc);
        cc.addConstructor(constructor);

      } catch (CannotCompileException e) {
        System.err.println("Unable to apply injector to " + cc.getName() + ". Skipping");
        e.printStackTrace();
      }
    });*/

    return injectors;
  }

  private static Map<String, String> findClassMap(String osrsInjectedClientJarPath, URL[] osrsJars,  String rspsClientJarPath, URL[] rspsJars) {
    try {
      ClassLoader osrsLoader = new URLClassLoader(osrsJars, RSPSClientInjector.class.getClassLoader());
      ClassLoader rspsLoader = new URLClassLoader(rspsJars, RSPSClientInjector.class.getClassLoader());

      System.out.println("Class Mapping:");

      JarInjector.mapEntries(osrsInjectedClientJarPath, (osrsName, osrsData) -> {
        if (osrsName.endsWith(".class")) {
          String osrsClassName = osrsName.replace("/", ".").substring(0, osrsName.length() - ".class".length());
          try {
            Class<?> osrsClass = osrsLoader.loadClass(osrsClassName);
            JarInjector.mapEntries(rspsClientJarPath, (rspsName, rspsData) -> {
              if (rspsName.endsWith(".class")) {
                String rspsClassName = rspsName.replace("/", ".").substring(0, rspsName.length() - ".class".length());
                try {
                  Class<?> rspsClass = rspsLoader.loadClass(rspsClassName);
                  if (isEquivalentClass(osrsClass, rspsClass)) {
                    System.out.println(osrsClassName + " -> " + rspsClassName);
                  }
                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                  System.err.println("RSPS class " + rspsName + " could not be found");
                  //e.printStackTrace();
                }
              }
            });
          } catch (NoClassDefFoundError | ClassNotFoundException e) {
            System.err.println("OSRS class " + osrsClassName + " could not be found");
            e.printStackTrace();
          } catch (IOException e) {
            System.err.println("RSPS client classes could not be iterated over");
            e.printStackTrace();
          }
        }
      });
    } catch (IOException e) {
      System.err.println("OSRS to RSPS class mapping could not be found");
      e.printStackTrace();
    }

    return null;
  }

  private static boolean isEquivalentClass(Class<?> osrsClass, Class<?> rspsClass) {
    return hasEquivalentFields(osrsClass, rspsClass) &&
           hasEquivalentMethods(osrsClass, rspsClass);
  }

  private static boolean hasEquivalentFields(Class<?> osrsClass, Class<?> rspsClass) {
    if (osrsClass.getDeclaredFields().length != rspsClass.getDeclaredFields().length) {
      return false;
    }

    List<Field> usedFields = new ArrayList<>();

    for (Field osrsField : osrsClass.getDeclaredFields()) {
      boolean foundField = false;
      for (Field rspsField : rspsClass.getDeclaredFields()) {
        if (!usedFields.contains(rspsField) &&
            osrsField.getModifiers() == rspsField.getModifiers() &&
            areEquivalentTypes(osrsField.getType(), rspsField.getType())) {

          foundField = true;
          usedFields.add(rspsField);
          break;
        }
      }

      if (!foundField) {
        return false;
      }
    }

    return true;
  }

  private static boolean hasEquivalentMethods(Class<?> osrsClass, Class<?> rspsClass) {
    if (osrsClass.getDeclaredMethods().length != rspsClass.getDeclaredMethods().length) {
      return false;
    }

    List<Method> usedMethods = new ArrayList<>();

    for (Method osrsMethod : osrsClass.getDeclaredMethods()) {
      boolean foundMethod = false;
      for (Method rspsMethod : rspsClass.getDeclaredMethods()) {
        if (!usedMethods.contains(rspsMethod) &&
            osrsMethod.getModifiers() == rspsMethod.getModifiers() &&
            areEquivalentTypes(osrsMethod.getReturnType(), rspsMethod.getReturnType()) &&
            osrsMethod.getParameterCount() == rspsMethod.getParameterCount()) {

          boolean paramsEqual = true;

          for (int i = 0; i < osrsMethod.getParameterCount(); i++) {
            if (!areEquivalentTypes(osrsMethod.getParameterTypes()[i], rspsMethod.getParameterTypes()[i])) {
              paramsEqual = false;
              break;
            }
          }

          if (paramsEqual) {
            foundMethod = true;
            usedMethods.add(rspsMethod);
            break;
          }
        }
      }

      if (!foundMethod) {
        return false;
      }
    }

    return true;
  }

  private static boolean areEquivalentTypes(Class<?> c1, Class<?> c2) {
    if (isClientClass(c1) && isClientClass(c2)) { // both unmapped client classes, just assume they are equal
      return true;
    }

    return c1.equals(c2);
  }

  private static boolean isClientClass(Class<?> clazz) { // is unmapped client class (either in OSRS client or RSPS client)
    return !clazz.isPrimitive() && clazz.getPackage() == null;
  }

}
