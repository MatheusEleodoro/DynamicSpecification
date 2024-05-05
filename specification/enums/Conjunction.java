package br.com.evertec.sinqia.contabil.specification.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Conjunction {
  AND("And"),
  OR("Or");
  private final String description;
}
