package net.rspslite.rsps.hooks;

import java.util.Map;
import java.util.Arrays;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtConstructor;
import javassist.CtBehavior;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;

public class FieldFinder {

  private String type;
  private String field;

  // method and bytecode fields only used for type "method" field finder
  private String method;
  private String[] bytecode;

  // paramTypes field only used for type "constructor" field finder
  private String[] paramTypes;

  public String getType() {
    return type;
  }

  public String getField() {
    return field;
  }

  public String getMethod() {
    return method;
  }

  public String[] getBytecode() {
    return bytecode;
  }

  public String[] getParamTypes() {
    return paramTypes;
  }

  public void find(CtClass cc, String[] fields, Map<String, CtMethod> methodMap, Map<String, CtField> fieldMap) {
    switch (getType()) {
      case "method":
        findByMethodExamination(cc, methodMap, fieldMap);
        break;
      case "constructor":
        findByConstructorExamination(cc, fieldMap);
        break;
      case "logical_elimination":
        findByLogicalElimination(cc, fields, fieldMap);
        break;
      default:
        System.err.println("Couldn't map field " + getField() + " of class " + cc.getName());
        break;
    }
  }

  private void findByMethodExamination(CtClass cc, Map<String, CtMethod> methodMap, Map<String, CtField> fieldMap) {
    CtMethod method = MethodSignatureUtil.getMethod(getMethod(), methodMap);

    if (method != null) {
      findByBehaviorExamination(cc, method, fieldMap);
    }
    else {
      System.err.println("FieldFinder method examination method not found in map for field " + getField() + " in " + cc.getName());
    }
  }

  private void findByConstructorExamination(CtClass cc, Map<String, CtField> fieldMap) {
    CtConstructor constructor = HookConstructor.findConstructorByParamTypes(cc, getParamTypes());

    if (constructor != null) {
      findByBehaviorExamination(cc, constructor, fieldMap);
    }
    else {
      System.err.println("FieldFinder constructor examination constructor not found in map for field " + getField() + " in " + cc.getName());
    }
  }

  private void findByBehaviorExamination(CtClass cc, CtBehavior behavior, Map<String, CtField> fieldMap) {
    CodeAttribute code = behavior.getMethodInfo().getCodeAttribute();

    int bytecodeIndex = BytecodeUtil.findBytecodePatternIndex(code, getBytecode());
    byte[] bytecodeBytes = code.getCode();

    if (bytecodeIndex == -1) {
      System.err.println("Couldn't find bytecode match");
      return;
    }

    // assumes the bytecode pattern (getBytecode()) ends with a getfield or putfield
    // getfield and putfield have a 16-bit unsigned integer index after the opcode
    int constPoolIndex = ((bytecodeBytes[bytecodeIndex+1] & 0xFF) << 8) + (bytecodeBytes[bytecodeIndex+2] & 0xFF); // & 0xFF converts to unsigned byte (as int type so it can store it)

    ConstPool constPool = code.getConstPool();
    String fieldName = constPool.getFieldrefName(constPoolIndex);

    try {
      CtField fieldObj = cc.getDeclaredField(fieldName);
      fieldMap.put(getField(), fieldObj);
    } catch (NotFoundException e) {
      e.printStackTrace();
      System.err.println("FieldFinder method examination failed getting found field for field " + getField() + " in " + cc.getName());
      return;
    }
  }

  private void findByLogicalElimination(CtClass cc, String[] fieldSignatures, Map<String, CtField> fieldMap) {
    String fieldType = getFieldType(cc.getName(), getField(), fieldSignatures);

    if (fieldType == null) {
      System.err.println("FieldFinder logical elimination failed for field " + getField() + " in " + cc.getName());
      return;
    }

    CtField[] possibleFields = getPossibleFields(cc, fieldType, fieldMap);

    if (possibleFields.length == 1) {
      fieldMap.put(getField(), possibleFields[0]);
    }
    else {
      System.err.println("FieldFinder logical elimination only able to narrow it down to " + possibleFields.length + " possible fields");
      return;
    }
  }

  private static CtField[] getPossibleFields(CtClass cc, String fieldType, Map<String, CtField> fieldMap) {
    return Arrays.stream(cc.getDeclaredFields())
      .filter(f -> !Modifier.isStatic(f.getModifiers()))
      .filter(f -> {
        try {
          return f.getType().getName().equals(fieldType);
        } catch (NotFoundException e) {
          e.printStackTrace();
          return false;
        }
      })
      .filter(f -> !fieldMap.containsValue(f))
      .toArray(CtField[]::new);
  }

  private static String getFieldType(String className, String searchFieldName, String[] fieldSignatures) {
    for (String hookField : fieldSignatures) {
      String[] splitString = hookField.split("[\\s]+");
      String fieldType = splitString[0];
      String fieldName = splitString[1];

      if (fieldType.equals("$$thisClass$$")) {
        fieldType = className;
      }

      if (fieldName.equals(searchFieldName)) {
        return fieldType;
      }
    }

    return null;
  }

}
