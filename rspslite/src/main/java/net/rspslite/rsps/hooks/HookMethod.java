package net.rspslite.rsps.hooks;

import java.util.List;
import java.util.ArrayList;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.BadBytecode;

public class HookMethod {

  private String signature;
  private String[][] bytecode;

  public String getSignature() {
    return signature;
  }

  public String getReturnType() {
    return getSignature().split("[\\s]+")[0];
  }

  public String getAssignedName() {
    return getSignature().split("[\\s]+")[1].split("\\(")[0];
  }

  public String[] getParameterTypes() {
    if (getSignature().contains("()")) {
      return new String[]{};
    }
    return getSignature().split("\\(")[1].split("\\)")[0].split(",[\\s]*");
  }

  public String[][] getBytecode() {
    return bytecode;
  }

  public boolean isMatch(CtMethod method) {
    try {
      if (!method.getReturnType().getName().equals(getReturnType())) {
        return false;
      }

      if (method.getParameterTypes().length != this.getParameterTypes().length) {
        return false;
      }

      for (int i = 0; i < this.getParameterTypes().length; i++) {
        String thisParamType = this.getParameterTypes()[i];
        String methodParamType = method.getParameterTypes()[i].getName();
        if (!methodParamType.equals(thisParamType)) {
          return false;
        }
      }
    } catch (NotFoundException e) {
      e.printStackTrace();
      return false;
    }

    return isBytecodeMatch(method);
  }

  private boolean isBytecodeMatch(CtMethod method) {
    CodeAttribute code = method.getMethodInfo().getCodeAttribute();

    if (code == null) {
      return false;
    }

    for (String[] bytecodeBlock : this.getBytecode()) {
      if (BytecodeUtil.findBytecodePatternIndex(code, bytecodeBlock) == -1) {
        return false;
      }
    }

    return true;
  }

}
