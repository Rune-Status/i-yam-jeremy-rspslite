package net.rspslite.rsps.hooks;

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

}
