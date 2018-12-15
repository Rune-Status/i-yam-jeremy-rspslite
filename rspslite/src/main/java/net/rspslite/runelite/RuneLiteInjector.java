package net.rspslite.runelite;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import javassist.CtClass;

public class RuneLiteInjector {

  public static Map<String, Consumer<CtClass>> getInjectors() {
    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    injectors.put("net.runelite.launcher.Launcher", (cc) -> {
      System.out.println("Injector called on " + cc.getName());
    });

    return injectors;
  }

}
