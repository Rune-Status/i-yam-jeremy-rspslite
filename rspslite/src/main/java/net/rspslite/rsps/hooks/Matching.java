package net.rspslite.rsps.hooks;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.NotFoundException;

public class Matching {

  private String childOf;
  private String[] fields;
  private HookMethod[] methods;
  private boolean isRootClass;

  public String getChildOf() {
    return childOf;
  }

  public String[] getFields() {
    return fields;
  }

  public HookMethod[] getMethods() {
    return methods;
  }

  public boolean isRootClass() {
    return isRootClass;
  }

  public boolean isMatch(CtClass cc, Map<String, CtClass> classMap, Map<String, CtMethod> methodMap) {
    return isMatchChildOf(cc, classMap) && isMatchRootClass(cc) && isMatchFields(cc) && isMatchMethods(cc, methodMap);
  }

  private boolean isMatchChildOf(CtClass cc, Map<String, CtClass> classMap) {
    if (getChildOf() == null) {
      return true;
    }

    String parentAssignedName = getChildOf();
    CtClass parentClass = classMap.get(parentAssignedName);
    String parentName = parentClass.getName();

    try {
      cc = cc.getSuperclass();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return false;
    }

    while (cc != null) {
      if (cc.getName().equals(parentName)) {
        return true;
      }
      try {
        cc = cc.getSuperclass();
      } catch (NotFoundException e) {
        e.printStackTrace();
        return false;
      }
    }

    return false;
  }

  private boolean isMatchRootClass(CtClass cc) {
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

  private boolean isMatchFields(CtClass cc) {
    if (getFields() == null) {
      return true;
    }

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

  private boolean isMatchMethods(CtClass cc, Map<String, CtMethod> methodMap) {
    if (getMethods() == null) {
      return true;
    }

    List<CtMethod> usedMethods = new ArrayList<>();

    for (HookMethod hookMethod : getMethods()) {
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
