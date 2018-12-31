package net.rspslite.rsps.hooks;

import java.util.Map;
import java.util.HashMap;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class Hook {

  private String name;
  private Matching matching;
  private FieldFinder[] fieldFinders;
  private Injections injections;

  public String getName() {
    return name;
  }

  public Matching getMatching() {
    return matching;
  }

  public FieldFinder[] getFieldFinders() {
    return fieldFinders;
  }

  public Injections getInjections() {
    return injections;
  }

  private boolean isMatch(CtClass cc, Map<String, CtMethod> methodMap) {
    return matching.isMatch(cc, methodMap);
  }

  public void tryApplyTo(CtClass cc) { // assumes the class is a match
    Map<String, CtMethod> methodMap = new HashMap<>();
    if (isMatch(cc, methodMap)) {
      for (String interfaceName : getInjections().getInterfaces()) {
        System.out.print(interfaceName + " ");
      }
      System.out.println("-> " + cc.getName());

      Map<String, CtField> fieldMap = new HashMap<>();

      for (FieldFinder finder : getFieldFinders()) {
        finder.find(cc, getMatching().getFields(), methodMap, fieldMap);
      }
    }
  }

}
