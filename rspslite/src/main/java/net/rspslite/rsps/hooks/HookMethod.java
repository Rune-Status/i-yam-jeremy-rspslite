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
    try {
      CodeAttribute code = method.getMethodInfo().getCodeAttribute();

      for (String[] bytecodeBlock : this.getBytecode()) {
        CodeIterator ci = code.iterator();
        boolean foundMatch = false;

        List<String> bytecodeBuffer = new ArrayList<>();
        while (ci.hasNext()) {
          String inst = opcodeToString(code.getCode()[ci.next()]);
          bytecodeBuffer.add(inst);

          if (bytecodeBuffer.size() > bytecodeBlock.length) {
              bytecodeBuffer.remove(0);
          }

          if (bytecodeBuffer.size() == bytecodeBlock.length) {
            if (bytecodeBlocksEqual(bytecodeBuffer.toArray(new String[]{}), bytecodeBlock)) {
              foundMatch = true;
              break;
            }
          }
        }

        if (!foundMatch) {
          return false;
        }
      }
    } catch (BadBytecode e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  private static String opcodeToString(byte opcode) {
    int index = opcode & 0xFF; // gets unsigned byte value
    return Mnemonic.OPCODE[index];
  }

  private static boolean bytecodeBlocksEqual(String[] bc1, String[] bc2) {
    if (bc1.length != bc2.length) {
      return false;
    }

    for (int i = 0; i < bc1.length; i++) {
      if ((bc1[i] == null && bc2[i] != null) ||
          (bc1[i] != null && bc2[i] == null) ||
          (!bc1[i].equals(bc2[i]))) {

        return false;
      }
    }

    return true;
  }

}
