package br.com.evertec.sinqia.contabil.specification;

import org.springframework.data.jpa.domain.Specification;


public interface AbstractFilter<T> {
  Specification<T> apply();
}
