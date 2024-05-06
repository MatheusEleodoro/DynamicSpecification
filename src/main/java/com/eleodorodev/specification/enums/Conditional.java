package com.eleodorodev.specification.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Conditional {
  EQ("Equals"),
  LK("Like"),
  CT("Contains"),
  BW("Between"),
  GT("GreaterThan"),
  LT("LessThan"),
  LTE("LessThanEqualTo"),
  GTE("GreaterThanEqualTo"),
  NOT("Negate");
  private final String description;
}
