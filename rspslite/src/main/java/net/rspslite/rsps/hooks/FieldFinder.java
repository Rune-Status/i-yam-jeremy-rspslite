package net.rspslite.rsps.hooks;

import java.util.Map;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class FieldFinder {

  private String type;
  private String field;

  // method and bytecode fields only used for type "method" field finder
  private String method;
  private String[] bytecode;



  public String getType() {
    return type;
  }

  public String getField() {
    return field;
  }

  public String getMethod() {
    return method;
  }

  public String[] getBytecode() {
    return bytecode;
  }


  public void find(CtClass cc, String[] fields, Map<String, CtField> fieldMap) {
    switch (getType()) {
      case "method":
        findByMethodExamination(cc, fields, fieldMap);
        break;
      case "logical_elimination":
        findByLogicalElimination(cc, fields, fieldMap);
        break;
      default:
        System.err.println("Couldn't map field " + getField() + " of class " + cc.getName());
        break;
    }
  }

  private void findByMethodExamination(CtClass cc, String[] fields, Map<String, CtField> fieldMap) {
    //TODO
    // getfield has a 16-bit unsigned integer index after it
  }

  private void findByLogicalElimination(CtClass cc, String[] fields, Map<String, CtField> fieldMap) {
    //TODO
  }

}
