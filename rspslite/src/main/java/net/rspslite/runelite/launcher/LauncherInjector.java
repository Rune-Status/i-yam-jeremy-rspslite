package net.rspslite.runelite.launcher;

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

public class LauncherInjector {

  public static void testMethod() {
    System.out.println("Test called!");
  }

  public static Map<String, Consumer<CtClass>> getInjectors() {
    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    injectors.put("net.runelite.launcher.Launcher", (cc) -> {
      try {
        ClassPool cp = cc.getClassPool();

        // Disable https for download bootstrap.json
        CtMethod getBootstrap = cc.getDeclaredMethod("getBootstrap", new CtClass[]{});
        getBootstrap.instrument(new ExprEditor() {

          @Override
          public void edit(NewExpr expr) throws CannotCompileException {
            if (expr.getClassName().equals("java.net.URL")) {
              System.out.println("Fixed URL to use http instead of https");
              expr.replace("$_ = new java.net.URL($proceed($$).toString().replace(\"https://\", \"http://\"));");
            }
          }

        });

        getBootstrap.insertBefore("net.rspslite.runelite.launcher.LauncherInjector.testMethod();");


        // Disable artifact JAR verification
        CtMethod verifyJarHashes = cc.getDeclaredMethod("verifyJarHashes", new CtClass[]{cp.get("net.runelite.launcher.beans.Artifact[]")});
        verifyJarHashes.insertBefore("return;");

        // Replace RuneLite client with injected Runelite client
        CtMethod download = cc.getDeclaredMethod("download", new CtClass[]{cp.get("net.runelite.launcher.LauncherFrame"), cp.get("net.runelite.launcher.beans.Bootstrap")});
        download.instrument(new ExprEditor() {

            @Override
            public void edit(MethodCall m) throws CannotCompileException {
              if (m.getMethodName().equals("getPath")) {
                System.out.println("Replace getPath()");
                m.replace("String path = $proceed($$); $_ = path.startsWith(\"http://repo.runelite.net/net/runelite/client/\") ? \"TODO\" : path;");
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

}
