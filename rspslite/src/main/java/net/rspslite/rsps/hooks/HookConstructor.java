package net.rspslite.rsps.hooks;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;

public class HookConstructor {

  private String[] paramTypes;
  private String[][] bytecode;

  public String[] getParamTypes() {
    return paramTypes;
  }

  public String[][] getBytecode() {
    return bytecode;
  }


  public boolean isDeclaredIn(CtClass cc) {
    CtConstructor constructor = findConstructorByParamTypes(cc, getParamTypes());
    if (constructor == null) {
      return false;
    }

    CodeAttribute code = constructor.getMethodInfo().getCodeAttribute();
    if (code == null) {
      return false;
    }

    for (String[] bytecodeBlock : getBytecode()) {
      if (BytecodeUtil.findBytecodePatternIndex(code, bytecodeBlock) == -1) {
        return false;
      }
    }

    return true;
  }

  public static CtConstructor findConstructorByParamTypes(CtClass cc, String[] paramTypeNames) {
    CtClass[] paramTypes = new CtClass[paramTypeNames.length];
    for (int i = 0; i < paramTypes.length; i++) {
      try {
        paramTypes[i] = cc.getClassPool().get(paramTypeNames[i]);
      } catch (NotFoundException e) {
        e.printStackTrace();
        System.err.println("Unknown class " + paramTypeNames[i]);
        return null;
      }
    }

    try {
      return cc.getDeclaredConstructor(paramTypes);
    } catch (NotFoundException e) {
      return null;
    }
  }

}
