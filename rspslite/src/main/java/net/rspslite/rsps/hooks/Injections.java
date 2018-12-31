package net.rspslite.rsps.hooks;

import java.util.Map;
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

    for (String newMethod : getNewMethods()) {
      for (String methodSignature : methodMap.keySet()) {
        newMethod = newMethod.replace("$$method:" + methodSignature + "$$", "$0." + methodMap.get(methodSignature).getName());
      }
      for (String fieldName : fieldMap.keySet()) {
        newMethod = newMethod.replace("$$field:" + fieldName + "$$", "$0." + fieldMap.get(fieldName).getName());
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
