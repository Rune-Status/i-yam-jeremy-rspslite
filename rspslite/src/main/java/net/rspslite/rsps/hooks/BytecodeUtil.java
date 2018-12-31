package net.rspslite.rsps.hooks;

import java.util.List;
import java.util.ArrayList;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.BadBytecode;

public class BytecodeUtil {

  public static int findBytecodePatternIndex(CodeAttribute code, String[] bytecodeBlock) {
    if (code == null) {
      return -1;
    }

    try {
      CodeIterator ci = code.iterator();

      List<String> bytecodeBuffer = new ArrayList<>();
      while (ci.hasNext()) {
        int index = ci.next();
        String inst = opcodeToString(code.getCode()[index]);
        bytecodeBuffer.add(inst);

        if (bytecodeBuffer.size() > bytecodeBlock.length) {
            bytecodeBuffer.remove(0);
        }

        if (bytecodeBuffer.size() == bytecodeBlock.length) {
          if (bytecodeBlocksEqual(bytecodeBuffer.toArray(new String[]{}), bytecodeBlock)) {
            return index;
          }
        }
      }
    } catch (BadBytecode e) {
      e.printStackTrace();
      return -1;
    }

    return -1;
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
