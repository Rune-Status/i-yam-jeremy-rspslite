package net.rspslite.rsps.hooks;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;

public class Matching {

  private String childOf;
  private String[] interfaces;
  private String[] fields;
  private boolean noAdditionalFields;
  private HookMethod[] methods;
  private HookConstructor[] constructors;
  private boolean isRootClass;

  public String getChildOf() {
    return childOf;
  }

  public String[] getInterfaces() {
    return interfaces;
  }

  public String[] getFields(CtClass cc, Map<String, CtClass> classMap) {
    if (fields == null) {
      return null;
    }

    String[] output = new String[fields.length];

    for (int i = 0; i < fields.length; i++) {
      String[] splitString = fields[i].split("[\\s]+");
      String fieldType = splitString[0];
      String fieldName = splitString[1];
      if (fieldType.equals("$$thisClass$$")) {
        fieldType = cc.getName();
      }
      else if (fieldType.startsWith("$$class:")) {
        String name = fieldType.substring("$$class:".length(), fieldType.length() - "$$".length());
        if (classMap.containsKey(name)) {
          fieldType = classMap.get(name).getName();
        }
        else {
          throw new RuntimeException("Class " + name + " not in class map at time of $$class:<class_name>$$ usage");
        }
      }

      output[i] = fieldType + " " + fieldName;
    }

    return output;
  }

  public boolean hasNoAdditionalFields() {
    return noAdditionalFields;
  }

  public HookMethod[] getMethods() {
    return methods;
  }

  public HookConstructor[] getConstructors() {
    return constructors;
  }

  public boolean isRootClass() {
    return isRootClass;
  }

  public boolean isMatch(CtClass cc, Map<String, CtClass> classMap, Map<String, CtMethod> methodMap) {
    return isMatchChildOf(cc, classMap) &&
     isMatchInterfaces(cc) &&
     isMatchRootClass(cc) &&
     isMatchFields(cc, classMap) &&
     isMatchMethods(cc, methodMap) &&
     isMatchConstructors(cc);
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

  private boolean isMatchInterfaces(CtClass cc) {
    if (getInterfaces() == null) {
      return true;
    }

    for (String interfaceName : getInterfaces()) {
      boolean foundMatch = false;
      try {
        for (CtClass interfaceClass : cc.getInterfaces()) {
          if (interfaceClass.getName().equals(interfaceName)) {
            foundMatch = true;
            break;
          }
        }
      } catch (NotFoundException e) {
        e.printStackTrace();
        return false;
      }

      if (!foundMatch) {
        return false;
      }
    }

    return true;
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

  private boolean isMatchFields(CtClass cc, Map<String, CtClass> classMap) {
    String[] hookFields = getFields(cc, classMap);
    if (hookFields == null) {
      return true;
    }
    if (hasNoAdditionalFields() && hookFields.length != cc.getDeclaredFields().length) {
      return false;
    }

    List<CtField> usedFields = new ArrayList<>();

    for (String hookField : hookFields) {
      String[] splitString = hookField.split("[\\s]+");
      String fieldType = splitString[0];
      String fieldName = splitString[1];

      boolean foundMatch = false;
      for (CtField field : cc.getDeclaredFields()) {
        try {
          if (!Modifier.isStatic(field.getModifiers()) &&
              !usedFields.contains(field) &&
              field.getType().getName().equals(fieldType)) {
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
        if (!Modifier.isStatic(method.getModifiers()) &&
            !usedMethods.contains(method) &&
            hookMethod.isMatch(method)) {
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

  private boolean isMatchConstructors(CtClass cc) {
    if (getConstructors() == null) {
      return true;
    }

    // no need to keep track of used constructors because each constructor with param types is unique
    for (HookConstructor hookConstructor : getConstructors()) {
      if (!hookConstructor.isDeclaredIn(cc)) {
        return false;
      }
    }

    return true;
  }

}
