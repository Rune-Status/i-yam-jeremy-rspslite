package net.rspslite.rsps;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javassist.CtClass;
import javassist.ClassPool;
import javassist.CtMethod;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.NotFoundException;
import javassist.CannotCompileException;
import net.rspslite.injector.JarInjector;

public class RSPSClientInjector {

  public static Map<String, Consumer<CtClass>> getInjectors(String osrsInjectedClientJarPath, URL[] osrsJars, String rspsClientJarPath, URL[] rspsJars) {

    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    //Map<String, String> oldschoolToRSPSClassMap = findClassMap(osrsInjectedClientJarPath, osrsJars, rspsClientJarPath, rspsJars);

    injectors.put("Alora", (cc) -> {
      try {
        ClassPool cp = cc.getClassPool();

        CtConstructor constructor = CtNewConstructor.make("public Alora() { this(\"1\", true); }", cc);
        cc.addConstructor(constructor);

        CtMethod createFrame = cc.getDeclaredMethod("createFrame", new CtClass[]{CtClass.booleanType});
        createFrame.instrument(new ExprEditor() {

          @Override
          public void edit(MethodCall m) throws CannotCompileException {
            if (m.getMethodName().equals("setVisible")) {
              m.replace("{ }");
            }
          }

        });

      } catch (NotFoundException | CannotCompileException e) {
        System.err.println("Unable to apply injector to " + cc.getName() + ". Skipping");
        e.printStackTrace();
      }
    });

    return injectors;
  }

  private static Map<String, String> findClassMap(String osrsInjectedClientJarPath, URL[] osrsJars,  String rspsClientJarPath, URL[] rspsJars) {
    Map<String, String> classMap = new HashMap<>();

    try {
      ClassLoader osrsLoader = new URLClassLoader(osrsJars, RSPSClientInjector.class.getClassLoader());
      ClassLoader rspsLoader = new URLClassLoader(rspsJars, RSPSClientInjector.class.getClassLoader());

      System.out.println("Class Mapping:");

      while (true) {
        System.out.println("MAP ITERATION");
        JarInjector.mapEntries(osrsInjectedClientJarPath, (osrsName, osrsData) -> {
          if (osrsName.endsWith(".class")) {
            String osrsClassName = osrsName.replace("/", ".").substring(0, osrsName.length() - ".class".length());

            if (!classMap.containsKey(osrsClassName)) {
              try {
                Class<?> osrsClass = osrsLoader.loadClass(osrsClassName);
                List<Class<?>> matchedClasses = new ArrayList<>();
                JarInjector.mapEntries(rspsClientJarPath, (rspsName, rspsData) -> {
                  if (rspsName.endsWith(".class")) {
                    String rspsClassName = rspsName.replace("/", ".").substring(0, rspsName.length() - ".class".length());
                    try {
                      Class<?> rspsClass = rspsLoader.loadClass(rspsClassName);
                      if (isEquivalentClass(osrsClass, rspsClass, classMap)) {
                        matchedClasses.add(rspsClass);
                      }
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                      //System.err.println("RSPS class " + rspsName + " could not be found");
                      //e.printStackTrace();
                    }
                  }
                });

                if (matchedClasses.size() == 1) {
                  String rspsClassName = matchedClasses.get(0).getName();
                  classMap.put(osrsClassName, rspsClassName);
                  System.out.println(osrsClassName + " -> " + rspsClassName);
                }
              } catch (NoClassDefFoundError | ClassNotFoundException e) {
                System.err.println("OSRS class " + osrsClassName + " could not be found");
                e.printStackTrace();
              } catch (IOException e) {
                System.err.println("RSPS client classes could not be iterated over");
                e.printStackTrace();
              }
            }
          }
        });

        try { Thread.sleep(250); } catch (Exception e) {}
      }
    } catch (IOException e) {
      System.err.println("OSRS to RSPS class mapping could not be found");
      e.printStackTrace();
    }

    return classMap;
  }

  private static boolean isEquivalentClass(Class<?> osrsClass, Class<?> rspsClass, Map<String, String> currentClassMap) {
    return hasEquivalentFields(osrsClass, rspsClass, currentClassMap) &&
           hasEquivalentMethods(osrsClass, rspsClass, currentClassMap);
  }

  private static boolean hasEquivalentFields(Class<?> osrsClass, Class<?> rspsClass, Map<String, String> currentClassMap) {
    Field[] osrsFields = getNonStaticFields(osrsClass);
    Field[] rspsFields = getNonStaticFields(rspsClass);

    if (osrsFields.length != rspsFields.length) {
      return false;
    }

    List<Field> usedFields = new ArrayList<>();

    for (Field osrsField : osrsFields) {
      boolean foundField = false;
      for (Field rspsField : rspsFields) {
        if (!usedFields.contains(rspsField) &&
            osrsField.getModifiers() == rspsField.getModifiers() &&
            areEquivalentTypes(osrsField.getType(), rspsField.getType(), currentClassMap)) {

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

  private static boolean hasEquivalentMethods(Class<?> osrsClass, Class<?> rspsClass, Map<String, String> currentClassMap) {
    Method[] osrsMethods = getNonStaticMethods(osrsClass);
    Method[] rspsMethods = getNonStaticMethods(rspsClass);

    if (osrsMethods.length != rspsMethods.length) {
      return false;
    }

    List<Method> usedMethods = new ArrayList<>();

    for (Method osrsMethod : osrsMethods) {
      boolean foundMethod = false;
      for (Method rspsMethod : rspsMethods) {
        if (!usedMethods.contains(rspsMethod) &&
            osrsMethod.getModifiers() == rspsMethod.getModifiers() &&
            areEquivalentTypes(osrsMethod.getReturnType(), rspsMethod.getReturnType(), currentClassMap) &&
            osrsMethod.getParameterCount() == rspsMethod.getParameterCount()) {

          boolean paramsEqual = true;

          for (int i = 0; i < osrsMethod.getParameterCount(); i++) {
            if (!areEquivalentTypes(osrsMethod.getParameterTypes()[i], rspsMethod.getParameterTypes()[i], currentClassMap)) {
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

  private static Field[] getNonStaticFields(Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).toArray(Field[]::new);
  }

  private static Method[] getNonStaticMethods(Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredMethods()).filter(m -> !Modifier.isStatic(m.getModifiers())).toArray(Method[]::new);
  }

  private static boolean areEquivalentTypes(Class<?> classFromOsrs, Class<?> classFromRsps, Map<String, String> currentClassMap) {
    if (currentClassMap.containsKey(classFromOsrs.getName())) {
      String rspsMappedClassName = currentClassMap.get(classFromOsrs.getName());
      return classFromRsps.getName().equals(rspsMappedClassName);
    }

    if (isClientClass(classFromOsrs) && isClientClass(classFromRsps)) { // both unmapped client classes, just assume they are equal
      return true;
    }

    return classFromOsrs.equals(classFromRsps);
  }

  private static boolean isClientClass(Class<?> clazz) { // is unmapped client class (either in OSRS client or RSPS client)
    return !clazz.isPrimitive() && clazz.getPackage() == null;
  }

}
