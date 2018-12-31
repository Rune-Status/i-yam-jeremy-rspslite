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
            newMethod = newMethod.replace(matcher.group(0), "$0." + MethodSignatureUtil.getMethod(value, methodMap).getName());
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

}
