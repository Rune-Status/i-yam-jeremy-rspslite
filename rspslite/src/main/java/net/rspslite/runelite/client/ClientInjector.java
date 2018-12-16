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

  public static Map<String, Consumer<CtClass>> getInjectors() {
    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    injectors.put("net.runelite.client.rs.ClientLoader", (cc) -> {
      try {

        CtMethod load = cc.getDeclaredMethod("load");
        load.insertBefore("");
        System.out.println("FOUND LOAD METHOD ON RUNELITE CLIENT");

      } catch (NotFoundException | CannotCompileException e) {
        System.err.println("Unable to apply injector to " + cc.getName() + ". Skipping");
        e.printStackTrace();
      }
    });

    return injectors;
  }

}
