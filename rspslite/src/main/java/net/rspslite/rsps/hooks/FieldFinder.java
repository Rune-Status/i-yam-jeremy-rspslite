package net.rspslite.rsps.hooks;

import java.util.Map;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;

public class FieldFinder {

  private String type;
  private String field;

  // method and bytecode fields only used for type "method" field finder
  private String method;
  private String[] bytecode;



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


  public void find(CtClass cc, String[] fields, Map<String, CtMethod> methodMap, Map<String, CtField> fieldMap) {
    switch (getType()) {
      case "method":
        findByMethodExamination(cc, fields, methodMap, fieldMap);
        break;
      case "logical_elimination":
        findByLogicalElimination(cc, fields, fieldMap);
        break;
      default:
        System.err.println("Couldn't map field " + getField() + " of class " + cc.getName());
        break;
    }
  }

  private void findByMethodExamination(CtClass cc, String[] fields, Map<String, CtMethod> methodMap, Map<String, CtField> fieldMap) {
    //TODO

    if (!methodMap.containsKey(getMethod())) {
      System.err.println("FieldFinder failed for field " + getField() + " in " + cc.getName());
    }

    CtMethod method = methodMap.get(getMethod());
    CodeAttribute code = method.getMethodInfo().getCodeAttribute();

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

    CtField fieldObj = cc.getDeclaredField(fieldName);
    fieldMap.put(getField(), fieldObj);
  }

  private void findByLogicalElimination(CtClass cc, String[] fields, Map<String, CtField> fieldMap) {
    //TODO
  }

}
