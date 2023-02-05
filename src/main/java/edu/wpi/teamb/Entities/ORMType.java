package edu.wpi.teamb.Entities;

public enum ORMType {
  PTREQUEST("patienttransportationrequest"),
  SREQUEST("SanitationRequest"),
  CREQUEST("ComputerHelpRequest"),
  NODE("Node"),
  EDGE("Edge"),
  LOCATIONNAME("LocationName"),
  LOGIN("Login"),
  MOVE("Move");

  private String str;

  ORMType(String str) {
    this.str = str;
  }

  public String toString() {
    return str;
  }
}
