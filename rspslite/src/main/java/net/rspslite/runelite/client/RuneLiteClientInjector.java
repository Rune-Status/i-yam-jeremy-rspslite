package net.rspslite.runelite.client;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.CannotCompileException;

public class RuneLiteClientInjector {

  public static Map<String, Consumer<CtClass>> getInjectors() {

    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    injectors.put("net.runelite.client.rs.ClientLoader", (cc) -> {
      try {

        CtMethod load = cc.getDeclaredMethod("load", new CtClass[]{});
        cc.removeMethod(load);

        CtMethod newLoad = CtMethod.make("public java.applet.Applet load() { return net.rspslite.rsps.RSPSClient.getApplet($0.getClass().getClassLoader()); }", cc);
        cc.addMethod(newLoad);

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
