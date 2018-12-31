package net.rspslite.rsps.hooks;

import java.util.Map;
import javassist.CtMethod;

public class MethodSignatureUtil {

  public static CtMethod getMethod(String methodSigWithoutReturnType, Map<String, CtMethod> methodMap) {
    String methodName = methodSigWithoutReturnType.split("[\\s]+")[0];
    String[] methodParamTypes = getMethodParamTypes(methodSigWithoutReturnType);
    for (String otherMethodSignature : methodMap.keySet()) {
      String otherMethodName = otherMethodSignature.split("[\\s]+")[1];
      String[] otherMethodParamTypes = getMethodParamTypes(otherMethodSignature);

      if (methodName.equals(otherMethodName) && areStringArraysEqual(methodParamTypes, otherMethodParamTypes)) {
        return methodMap.get(otherMethodSignature);
      }
    }

    System.err.println("Couldn't find method with signature " + methodSigWithoutReturnType + " in injection of new method");
    return null;
  }

  private static String[] getMethodParamTypes(String methodSignature) {
    String[] splitString = methodSignature.split("\\(")[1].split("\\)");
    if (splitString.length == 0) {
      return new String[]{};
    }
    else {
      return splitString[0].split(",[\\s]*");
    }
  }

  private static boolean areStringArraysEqual(String[] a, String[] b) {
    if (a.length != b.length) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if (!a[i].equals(b[i])) {
        return false;
      }
    }

    return true;
  }

}
