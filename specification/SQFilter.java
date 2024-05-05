package br.com.evertec.sinqia.contabil.specification;

import jakarta.persistence.criteria.*;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Filtros Criteria/Specification para consultadas dinamicas
 *
 * @author <a href="https://contacts.google.com/matheus.souza@.sinqia.com.br">Matheus Eleodoro</a>
 * @version 1.0
 * @apiNote <a href="https://tfs.seniorsolution.com.br/SQcwb/SQContabilidade/_git/sqct-contabilidade-rest?version=GBdevelop&_a=contents/">...</a>
 */
public interface SQFilter {
  /**
   * Filter Criteria/Specification Equals
   *
   * @param compare   - Objeto com o valor a ser comparado
   * @param attribute - nome do campo a ser comparados
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return predicates - specification Equals
   */
  static <R, T> SQSpecification<R> toEquals(T compare, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!isEmpty(compare)) {
        Path<T> campoId;
        if (parents != null) {
          Path<R> parentsPath = getPathRoot(root, parents);
          campoId = parentsPath.get(attribute);
        } else {
          campoId = root.get(attribute);
        }
        predicates.add(builder.equal(campoId, compare));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * Filter Criteria/Specification NotEquals
   *
   * @param compare   - Objeto com o valor a ser comparado
   * @param attribute - nome do campo a ser comparados
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return predicates - specification Equals
   */
  static <R, T> SQSpecification<R> toNotEquals(T compare, String attribute, String... parents) {
    return SQSpecification.not(toEquals(compare, attribute, parents));
  }


  /**
   * Filter Criteria/Specification Like
   *
   * @param compare   - Objeto com o valor a ser comparado
   * @param attribute - nome do campo a ser comparados
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return predicates - specification Like
   */
  static <T> SQSpecification<T> toLike(Object compare, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (StringUtils.hasText((String) compare)) {
        Path<String> campoCompare;
        if (parents != null) {
          Path<?> parentsPath = getPathRoot(root, parents);
          campoCompare = parentsPath.get(attribute);
        } else {
          campoCompare = root.get(attribute);
        }
        predicates.add(builder.like(builder.lower(campoCompare), "%" + compare.toString().toLowerCase(Locale.ROOT) + "%"));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * Filter Criteria/Specification Like
   *
   * @param compare   - Objeto com o valor a ser comparado
   * @param attribute - nome do campo a ser comparados
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return predicates - specification notLike
   */
  static <T> SQSpecification<T> toNotLike(Object compare, String attribute, String... parents) {
    return SQSpecification.not(toLike(compare, attribute, parents));
  }


  /**
   * Filter Criteria/Specification Between
   *
   * @param start     - Objeto inicial da comparação
   * @param end       - Objeto final da comparação
   * @param attribute - nome do a atributo a ser comparado Ex.("data")
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return - CriteriaSpecification
   */
  static <T extends Comparable<T>, R> SQSpecification<R> toBetween(T start, T end, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!isEmpty(start) && !isEmpty(end)) {
        Path<T> attr;
        if (parents != null) {
          Path<R> parentsPath = getPathRoot(root, parents);
          attr = parentsPath.get(attribute);
        } else {
          attr = root.get(attribute);
        }
        predicates.add(builder.between(attr, start, end));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * Filter Criteria/Specification Not Between
   *
   * @param start     - Objeto inicial da comparação
   * @param end       - Objeto final da comparação
   * @param attribute - nome do a atributo a ser comparado Ex.("data")
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return - CriteriaSpecification
   */
  static <T extends Comparable<T>, R> SQSpecification<R> toNotBetween(T start, T end, String attribute, String... parents) {
    return SQSpecification.not(toBetween(start, end, attribute, parents));
  }

  /**
   * Filter Criteria/Specification Contains
   *
   * @param compare   - Objeto com o valor a ser comparado
   * @param attribute - nome do atributo a ser consultado.
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return predicates - specification Equals
   */
  static <T, R> SQSpecification<R> toContains(List<T> compare, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!isEmpty(compare)) {
        Expression<T> campo;
        if (parents != null) {
          Path<R> parentsPath = getPathRoot(root, parents);
          campo = parentsPath.get(attribute);
          predicates.add(campo.in(compare));
        } else {
          campo = root.get(attribute);
        }
        predicates.add(campo.in(compare));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * Filter Criteria/Specification Not Contains
   *
   * @param compare   - Objeto com o valor a ser comparado
   * @param attribute - nome do atributo a ser consultado.
   * @param parents   - parametro opcional que devera ser informado os parentes de onde o atributo esta aninhado
   * @return predicates - specification Equals
   */
  static <T, R> SQSpecification<R> toNotContains(List<T> compare, String attribute, String... parents) {
    return SQSpecification.not(toContains(compare, attribute, parents));
  }


  static <T extends Comparable<T>, R> SQSpecification<R> toGreater(T compare, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!isEmpty(compare)) {
        Path<T> campoId;
        if (parents != null) {
          Path<R> parentsPath = getPathRoot(root, parents);
          campoId = parentsPath.get(attribute);
        } else {
          campoId = root.get(attribute);
        }
        predicates.add(builder.greaterThan(campoId, compare));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  static <T extends Comparable<T>, R> SQSpecification<R> toGreaterEqualTo(T compare, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!isEmpty(compare)) {
        Path<T> campoId;
        if (parents != null) {
          Path<R> parentsPath = getPathRoot(root, parents);
          campoId = parentsPath.get(attribute);
        } else {
          campoId = root.get(attribute);
        }
        predicates.add(builder.greaterThanOrEqualTo(campoId, compare));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  static <T extends Comparable<T>, R> SQSpecification<R> toLess(T compare, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!isEmpty(compare)) {
        Path<T> campoId;
        if (parents != null) {
          Path<R> parentsPath = getPathRoot(root, parents);
          campoId = parentsPath.get(attribute);
        } else {
          campoId = root.get(attribute);
        }
        predicates.add(builder.lessThan(campoId, compare));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  static <T extends Comparable<T>, R> SQSpecification<R> toLessEqualTo(T compare, String attribute, String... parents) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!isEmpty(compare)) {
        Path<T> campoId;
        if (parents != null) {
          Path<R> parentsPath = getPathRoot(root, parents);
          campoId = parentsPath.get(attribute);
        } else {
          campoId = root.get(attribute);
        }
        predicates.add(builder.lessThanOrEqualTo(campoId, compare));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  static <T> Path<T> getPathRoot(Root<T> root, String... parents) {
    From<?, T> from = root;
    for (String parent : parents) {
      from = from.join(parent);
    }
    return from;
  }
  static boolean isEmpty(Object obj) {
    return Objects.isNull(obj) || obj.toString().isEmpty() ||
      (obj instanceof Number number && number.intValue() == 0) ||
      (obj instanceof List<?> && ((List<?>) obj).isEmpty());
  }
  @SuppressWarnings("unchecked")
  static <T> List<T> castList(Object obj) {
      return obj instanceof List<?> ? (List<T>) new ArrayList<>((List<?>) obj) : Collections.emptyList();
  }
}
