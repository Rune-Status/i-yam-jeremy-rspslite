package net.rspslite.rsps.hooks;

import java.util.List;
import java.util.ArrayList;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class Matching {

  private String[] fields;
  private HookMethod[] methods;
  private boolean isRootClass;

  public String[] getFields() {
    return fields;
  }

  public HookMethod[] getMethods() {
    return methods;
  }

  public boolean isRootClass() {
    return isRootClass;
  }

  public boolean isMatch(CtClass cc) {

    try {
      String superclassName = cc.getSuperclass().getName();
      if (!isRootClass() && superclassName.equals("java.lang.Object")) {
        return false;
      }
      if (isRootClass() && !superclassName.equals("java.lang.Object")) {
        return false;
      }
    } catch (NotFoundException e) {
      e.printStackTrace();
      return false;
    }

    // TODO check fields

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
