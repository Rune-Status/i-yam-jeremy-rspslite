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

        // Disable artifact JAR verification
        CtMethod verifyJarHashes = cc.getDeclaredMethod("verifyJarHashes", new CtClass[]{cp.get("net.runelite.launcher.beans.Artifact[]")});
        verifyJarHashes.insertBefore("return;");

        // Replace RuneLite client with injected Runelite client
        CtMethod download = cc.getDeclaredMethod("download", new CtClass[]{cp.get("net.runelite.launcher.LauncherFrame"), cp.get("net.runelite.launcher.beans.Bootstrap")});
        download.insertAfter("net.runelite.launcher.beans.Artifact[] artifacts = $2.getArtifacts(); String[] jarDependencies = new String[artifacts.length]; String clientJarPath = null; for (int i = 0; i < artifacts.length; i++) { jarDependencies[i] = REPO_DIR.toString() + java.io.File.separator + artifacts[i].getName(); if (artifacts[i].getPath().startsWith(\"http://repo.runelite.net/net/runelite/client/\")) { clientJarPath = jarDependencies[i]; } } net.rspslite.runelite.client.RuneLiteClient.injectClient(clientJarPath, jarDependencies);");

      } catch (NotFoundException | CannotCompileException e) {
        System.err.println("Unable to apply injector to " + cc.getName() + ". Skipping");
        e.printStackTrace();
      }
    });

    return injectors;
  }

}
