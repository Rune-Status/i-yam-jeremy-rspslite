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

  public void tryApplyTo(CtClass cc, Map<String, CtClass> classMap) {
    Map<String, CtMethod> methodMap = new HashMap<>();
    if (getMatching().isMatch(cc, classMap, methodMap)) {
      classMap.put(getName(), cc);
      System.out.println(getName() + "-> " + cc.getName());

      Map<String, CtField> fieldMap = new HashMap<>();

      if (getFieldFinders() != null) {
        for (FieldFinder finder : getFieldFinders()) {
          finder.find(cc, getMatching().getFields(cc, classMap), methodMap, fieldMap);
        }
      }

      for (String field : fieldMap.keySet()) {
        System.out.println("\t" + field + " -> " + fieldMap.get(field).getName());
      }

      getInjections().apply(cc, methodMap, fieldMap);
    }
  }

}
