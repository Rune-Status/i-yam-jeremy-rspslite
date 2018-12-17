package net.rspslite.rsps;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.CannotCompileException;

public class RSPSClientInjector {

  public static Map<String, Consumer<CtClass>> getInjectors() {

    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    injectors.put("Alora", (cc) -> {
      try {

        CtConstructor constructor = CtNewConstructor.make("public Alora() { this(\"1\", true); }", cc);
        cc.addConstructor(constructor);

      } catch (CannotCompileException e) {
        System.err.println("Unable to apply injector to " + cc.getName() + ". Skipping");
        e.printStackTrace();
      }
    });

    return injectors;
  }

}
