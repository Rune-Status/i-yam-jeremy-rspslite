package net.rspslite.runelite.client;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import javassist.NotFoundException;
import javassist.CannotCompileException;

public class ClientInjector {

  public static Map<String, Consumer<CtClass>> getInjectors(String rspsClientJarUrl, String rspsClientMainClass) {

    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    /*injectors.put("net.runelite.client.rs.RSConfig", (cc) -> {
      try {

        CtMethod getCodeBase = cc.getDeclaredMethod("getCodeBase", new CtClass[]{});
        getCodeBase.insertBefore("return \"" + getCodeBase(rspsClientJarUrl) + "\";");

        CtMethod getInitialJar = cc.getDeclaredMethod("getInitialJar", new CtClass[]{});
        getInitialJar.insertBefore("return \"" + getInitialJar(rspsClientJarUrl) + "\";");

        CtMethod getInitialClass = cc.getDeclaredMethod("getInitialClass", new CtClass[]{});
        getInitialClass.insertBefore("return \"" + rspsClientMainClass + "\";");

      } catch (NotFoundException | CannotCompileException e) {
        System.err.println("Unable to apply injector to " + cc.getName() + ". Skipping");
        e.printStackTrace();
      }
    });*/

    injectors.put("net.runelite.client.rs.ClientLoader", (cc) -> {
      try {

        CtMethod load = cc.getDeclaredMethod("load");
        load.insertBefore("System.out.println(\"Hello\");");
        System.out.println("FOUND LOAD METHOD ON RUNELITE CLIENT");

      } catch (NotFoundException | CannotCompileException e) {
        System.err.println("Unable to apply injector to " + cc.getName() + ". Skipping");
        e.printStackTrace();
      }
    });

    return injectors;
  }

  private static String getCodeBase(String rspsClientJarUrl) {
    String initialJar = getInitialJar(rspsClientJarUrl);
    return rspsClientJarUrl.substring(0, rspsClientJarUrl.length() - initialJar.length());
  }

  private static String getInitialJar(String rspsClientJarUrl) {
    String[] split = rspsClientJarUrl.split("/");
    return split[split.length-1];
  }

}
