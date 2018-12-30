package net.rspslite.rsps.hooks;

import java.util.List;
import java.util.ArrayList;
import javassist.CtClass;
import javassist.CtMethod;

public class Matching {

  private String[] fields;
  private HookMethod[] methods;

  public String[] getFields() {
    return fields;
  }

  public HookMethod[] getMethods() {
    return methods;
  }

  public boolean isMatch(CtClass cc) {
    List<CtMethod> usedMethods = new ArrayList<>();

    for (HookMethod hookMethod : methods) {
      boolean foundMatch = false;
      for (CtMethod method : cc.getDeclaredMethods()) {
        if (!usedMethods.contains(method)) {
          if (hookMethod.isMatch(method)) {
            foundMatch = true;
            usedMethods.add(method);
            break;
          }
        }
      }

      if (!foundMatch) {
        return false;
      }
    }

    return true;
  }

}
