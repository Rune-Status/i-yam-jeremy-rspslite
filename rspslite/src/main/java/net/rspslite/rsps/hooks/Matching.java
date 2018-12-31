package net.rspslite.rsps.hooks;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
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

  public boolean isMatch(CtClass cc, Map<String, CtMethod> methodMap) {
    return isMatchRootClass(cc) && isMatchFields(cc) && isMatchMethods(cc, methodMap);
  }

  public boolean isMatchRootClass(CtClass cc) {
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

    return true;
  }

  public boolean isMatchFields(CtClass cc) {
    List<CtField> usedFields = new ArrayList<>();

    for (String hookField : getFields()) {
      String[] splitString = hookField.split("[\\s]+");
      String fieldType = splitString[0];
      String fieldName = splitString[1];

      if (fieldType.equals("$$thisClass$$")) {
        fieldType = cc.getName();
      }

      boolean foundMatch = false;
      for (CtField field : cc.getDeclaredFields()) {
        try {
          if (!usedFields.contains(field) && field.getType().getName().equals(fieldType)) {
            foundMatch = true;
            usedFields.add(field);
            break;
          }
        } catch (NotFoundException e) {
          e.printStackTrace();
        }
      }

      if (!foundMatch) {
        return false;
      }
    }

    return true;
  }

  public boolean isMatchMethods(CtClass cc, Map<String, CtMethod> methodMap) {
    List<CtMethod> usedMethods = new ArrayList<>();

    for (HookMethod hookMethod : methods) {
      boolean foundMatch = false;
      for (CtMethod method : cc.getDeclaredMethods()) {
        if (!usedMethods.contains(method) && hookMethod.isMatch(method)) {
          methodMap.put(hookMethod.getSignature(), method);
          foundMatch = true;
          usedMethods.add(method);
          break;
        }
      }

      if (!foundMatch) {
        return false;
      }
    }

    return true;
  }

}
