package net.rspslite.rsps.hooks;

import javassist.CtClass;

public class Hook {

  private Matching matching;
  private FieldFinder[] fieldFinders;
  private Injections injections;

  public Matching getMatching() {
    return matching;
  }

  public FieldFinder[] getFieldFinders() {
    return fieldFinders;
  }

  public Injections getInjections() {
    return injections;
  }

  public boolean isMatch(CtClass cc) {
    return matching.isMatch(cc);
  }

}
