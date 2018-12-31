package net.rspslite.rsps.hooks;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.CannotCompileException;

public class Injections {

  private String[] interfaces;
  private String[] newMethods;

  public String[] getInterfaces() {
    return interfaces;
  }

  public String[] getNewMethods() {
    return newMethods;
  }


  public void apply(CtClass cc, Map<String, CtMethod> methodMap, Map<String, CtField> fieldMap) {
    addInterfaces(cc);
    insertNewMethods(cc, methodMap, fieldMap);
  }

  private void addInterfaces(CtClass cc) {
    if (getInterfaces() == null) {
      return;
    }

    ClassPool cp = cc.getClassPool();
    for (String interfaceName : getInterfaces()) {
      try {
        cc.addInterface(cp.get(interfaceName));
      } catch (NotFoundException e) {
        e.printStackTrace();
        System.err.println("Unable to add interface " + interfaceName + " to " + cc.getName());
      }
    }
  }

  private void insertNewMethods(CtClass cc, Map<String, CtMethod> methodMap, Map<String, CtField> fieldMap) {
    if (getNewMethods() == null) {
      return;
    }

    //TODO make it not necessary to include return type in method signature when calling a method $$method:unlink()$$ instead of $$method:void unlink()$$ and make it work regardless of whitespace (so parse and match)
    Pattern pattern = Pattern.compile("\\$\\$(.*?)\\$\\$");

    for (String newMethod : getNewMethods()) {
      Matcher matcher = pattern.matcher(newMethod);

      while (matcher.find()) {
        String match = matcher.group(1);
        String[] args = match.split(":");
        String type = args[0];
        String value = args[1];

        switch (type) {
          case "field":
            newMethod = newMethod.replace(matcher.group(0), "$0." + fieldMap.get(value).getName());
            break;
          case "method":
            newMethod = newMethod.replace(matcher.group(0), "$0." + getMethodName(value, methodMap));
            break;
          default:
            System.err.println("Unrecognized $$ type: " + type + " in " + cc.getName());
            System.err.println(newMethod);
            break;
        }
      }

      try {
        cc.addMethod(CtMethod.make(newMethod, cc));
      } catch (CannotCompileException e) {
        e.printStackTrace();
        System.err.println("Method could not be added to " + cc.getName());
      }
    }
  }

  private static String getMethodName(String methodSigWithoutReturnType, Map<String, CtMethod> methodMap) {
    String methodName = methodSigWithoutReturnType.split("[\\s]+")[0];
    String[] methodParamTypes = getMethodParamTypes(methodSigWithoutReturnType);
    for (String otherMethodSignature : methodMap.keySet()) {
      String otherMethodName = otherMethodSignature.split("[\\s]+")[1];
      String[] otherMethodParamTypes = getMethodParamTypes(otherMethodSignature);

      if (methodName.equals(otherMethodName) && areStringArraysEqual(methodParamTypes, otherMethodParamTypes)) {
        return methodMap.get(otherMethodSignature).getName();
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
