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
import net.rspslite.rsps.hooks.HookReader;
import net.rspslite.rsps.hooks.Hook;

public class RSPSClientInjector {

  public static Map<String, Consumer<CtClass>> getInjectors(String osrsInjectedClientJarPath, URL[] osrsJars, String rspsClientJarPath, URL[] rspsJars) {

    Map<String, Consumer<CtClass>> injectors = new HashMap<>();

    //try { findNPCClass(rspsClientJarPath, rspsJars); } catch (IOException e) { e.printStackTrace(); }

    final Hook[] hooks = HookReader.readHooks();
    for (Hook hook : hooks) {
      System.out.println("Hook interface: " + hook.getInjections().getInterfaces()[0]);
    }

    injectors.put("Alora", (cc) -> {
      try {
        System.out.println("Hook #0 is match: " + hooks[0].isMatch(cc.getClassPool().get("ES")));
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        ClassPool cp = cc.getClassPool();

        CtClass clientClass = cp.get("net.runelite.rs.api.RSClient");

        cc.addInterface(clientClass);

        for (CtMethod method : clientClass.getMethods()) {
          if (!method.getDeclaringClass().getName().startsWith("net.runelite.")) {
            continue;
          }

          String[] params = new String[method.getParameterTypes().length];
          for (int i = 0; i < params.length; i++) {
            params[i] = method.getParameterTypes()[i].getName() + " param" + i;
          }
          String paramsString = String.join(", ", params);

          CtClass returnType = method.getReturnType();

          String src = "public " + returnType.getName() + " " + method.getName() + "(" + paramsString + ") {\n";

          if (method.getName().equals("getRevision")) {
            src += "return 317;";
          }
          else if (returnType.equals(CtClass.intType)) {
            src += "return 0;";
          }
          else if (returnType.equals(CtClass.longType)) {
            src += "return 0L;";
          }
          else if (returnType.equals(CtClass.booleanType)) {
            src += "return false;";
          }
          else if (returnType.equals(CtClass.voidType)) {
            src += "return;";
          }
          else {
            src += "return null;";
          }

          src += "\n}";

          cc.addMethod(CtMethod.make(src, cc));
        }

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

  private static void findNPCClass(String rspsClientJarPath, URL[] rspsJars) throws IOException {
    JarInjector.mapEntries(rspsClientJarPath, (rspsName, rspsData) -> {
      if (rspsName.endsWith(".class")) {
        String rspsClassName = rspsName.replace("/", ".").substring(0, rspsName.length() - ".class".length());
        try {
          javassist.bytecode.ClassFile cf = new javassist.bytecode.ClassFile(new java.io.DataInputStream(new java.io.ByteArrayInputStream(rspsData)));
          javassist.bytecode.ConstPool constants = cf.getConstPool();

          for (int i = 1; i < constants.getSize(); i++) {
            if (constants.getTag(i) == javassist.bytecode.ConstPool.CONST_String) { // or possibly CONST_Utf8
          		String s = constants.getStringInfo(i);
              if (s.equals("Vote manager")) {
                System.out.println("NPC Class: " + rspsClassName);
          		}
          	}
          }
        } catch (IOException | NoClassDefFoundError e) {
          //System.err.println("<>: RSPS class " + rspsName + " could not be found");
        }
      }
    });
  }

}
