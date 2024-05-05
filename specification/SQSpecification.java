package br.com.evertec.sinqia.contabil.specification;

import br.com.evertec.sinqia.contabil.specification.annotations.SpecAttr;
import br.com.evertec.sinqia.contabil.specification.enums.Conditional;
import br.com.evertec.sinqia.contabil.specification.enums.Conjunction;
import br.com.evertec.sinqia.contabil.specification.exception.SQSpecificationException;
import lombok.SneakyThrows;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.JpaSystemException;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filtros Criteria, Specification para consultadas dinamicas
 *
 * @author <a href="https://contacts.google.com/matheus.souza@.sinqia.com.br">Matheus Eleodoro</a>
 * @version 1.0
 * @apiNote <a href="https://tfs.seniorsolution.com.br/SQcwb/SQContabilidade/_git/sqct-contabilidade-rest?version=GBdevelop&_a=contents/">...</a>
 */
public interface SQSpecification<T> extends Specification<T> {

  static <T> SQSpecification<T> not(@Nullable Specification<T> spec) {
    return spec == null ? (root, query, builder) -> null : (root, query, builder) -> builder.not(spec.toPredicate(root, query, builder));
  }

  static <R, T> Specification<R> bind(Class<T> clazz, QueryParams querystring) throws SQSpecificationException {
    try {
      AtomicReference<Specification<R>> spec = new AtomicReference<>();
      AtomicBoolean first = new AtomicBoolean(true);
      var values = Stream.of(clazz.getDeclaredFields())
        .flatMap(field -> AnnotatedElementUtils.findAllMergedAnnotations(field, SpecAttr.class).stream()
          .map(annotation -> Map.entry(field, annotation)))
        .filter(n -> querystring.value().containsKey(n.getValue().property()) || querystring.value().containsKey(n.getValue().alias()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      values.forEach((key, specAttr) -> {
        Conditional conditional = specAttr.conditional();
        Conjunction conjunction = specAttr.conjunction();
        Pair<Object,String> params = querystring.value()
          .getOrDefault(specAttr.property(), querystring.value().get(specAttr.alias()));

        Object value = params.getFirst();
        boolean negate = specAttr.negate();

        if(querystring.searchURL() && !params.getSecond().isEmpty()){
          List<String> args = new ArrayList<>(Arrays.asList(params.getSecond().split(",")));
          List<String> validArgs = new ArrayList<>(Arrays.stream(Conjunction.values()).map(Enum::name).toList());
          validArgs.addAll(Arrays.stream(Conditional.values()).map(Enum::name).toList());

          args = args.stream().map(String::toUpperCase).toList();
          args = args.stream().filter(validArgs::contains).collect(Collectors.toList());

          if(args.size()==1){
            args.addFirst("AND");
          }

          args.sort(Comparator.comparing((String s) -> !s.equalsIgnoreCase("AND") && !s.equalsIgnoreCase("OR"))
            .thenComparing((String s) -> s.equalsIgnoreCase("NOT") ? 2 : 1));



          conjunction = Conjunction.valueOf(args.getFirst());
          conditional = Conditional.valueOf(args.get(1));

          negate = args.contains("NOT");
        }

        if (first.get()) {
          spec.set(apply(spec.get(),conditional,null, specAttr, value,negate));
          first.set(false);
          return;
        }
        spec.getAndSet(apply(spec.get(),conditional,conjunction, specAttr, value, negate));

      });
      return spec.get();
    } catch (JpaSystemException e) {
      throw new SQSpecificationException("Failed to generate Specification queries",e);
    }
  }


  static <T> Specification<T> apply(Specification<T> spec, Conditional conditional, Conjunction conjunction, SpecAttr specAttr,Object value,boolean negate) throws SQSpecificationException {
    try {
      String property = specAttr.property();
      String[] parents = specAttr.parents().length == 0 ? null : specAttr.parents();

      if (conjunction == null) return execWhere(negate, value, property, parents, conditional);

      return switch (conjunction) {
        case OR -> execOr(negate, value, property, parents, conditional, spec);
        case AND -> execAnd(negate, value, property, parents, conditional, spec);
      };
    } catch (JpaSystemException e) {
      throw new SQSpecificationException(e);
    }
  }

  static <T> Specification<T> execAnd(boolean negate, Object value, String property, String[] parents,
                                      Conditional conditional, Specification<T> spec) throws SQSpecificationException{
    try {
      List<Long> element = SQFilter.castList(value);
      return switch (conditional) {
        case LK  -> spec.and(negate ? SQFilter.toNotLike(value, property, parents) : SQFilter.toLike(value, property, parents));
        case CT  -> spec.and(negate ? SQFilter.toNotContains(SQFilter.castList(value), property, parents) : SQFilter.toContains(SQFilter.castList(value), property, parents));
        case BW  -> spec.and(negate ? SQFilter.toNotBetween(element.getFirst(), element.get(1), property, parents) : SQFilter.toBetween(element.getFirst(), element.get(1), property, parents));
        case GT  -> spec.and(SQFilter.toGreater(((Long) value),property,parents));
        case GTE -> spec.and(SQFilter.toGreaterEqualTo(((Long) value),property,parents));
        case LT  -> spec.and(SQFilter.toLess(((Long) value),property,parents));
        case LTE -> spec.and(SQFilter.toLessEqualTo(((Long) value),property,parents));
        default  -> spec.and(negate ? SQFilter.toNotEquals(value, property, parents) : SQFilter.toEquals(value, property, parents));
      };
    } catch (RuntimeException e) {
      throw new SQSpecificationException(e);
    }
  }

  static <T> Specification<T> execOr(boolean negate, Object value, String property, String[] parents,
                                     Conditional conditional, Specification<T> spec) throws SQSpecificationException {

    try {
      List<Long> element = SQFilter.castList(value);
      return switch (conditional) {
        case LK  -> spec.or(negate ? SQFilter.toNotLike(value, property, parents) : SQFilter.toLike(value, property, parents));
        case CT  -> spec.or(negate ? SQFilter.toNotContains(SQFilter.castList(value), property, parents) : SQFilter.toContains(SQFilter.castList(value), property, parents));
        case BW  -> spec.or(negate ? SQFilter.toNotBetween(element.getFirst(), element.get(1), property, parents) : SQFilter.toBetween(element.getFirst(), element.get(1), property, parents));
        case GT  -> spec.or(SQFilter.toGreater(((Long) value),property,parents));
        case GTE -> spec.or(SQFilter.toGreaterEqualTo(((Long) value),property,parents));
        case LT  -> spec.or(SQFilter.toLess(((Long) value),property,parents));
        case LTE -> spec.or(SQFilter.toLessEqualTo(((Long) value),property,parents));
        default  -> spec.or(negate ? SQFilter.toNotEquals(value, property, parents) : SQFilter.toEquals(value, property, parents));
      };
    } catch (JpaSystemException e) {
      throw new SQSpecificationException(e);
    }
  }

  static <T> Specification<T> execWhere(boolean negate, Object value, String property, String[] parents,
                                        Conditional conditional) throws SQSpecificationException {
    try {
      List<Long> element = SQFilter.castList(value);
      return switch (conditional) {
        case LK -> Specification.where(negate ? SQFilter.toNotLike(value, property, parents) : SQFilter.toLike(value, property, parents));
        case CT ->
          Specification.where(negate ? SQFilter.toNotContains(((List<?>) value), property, parents) : SQFilter.toContains(((List<?>) value), property, parents));
        case BW ->
          Specification.where(negate ? SQFilter.toNotBetween(element.getFirst(), element.get(1), property, parents) : SQFilter.toBetween(element.getFirst(), element.get(1), property, parents));
        case GT -> Specification.where(SQFilter.toGreater(((Long) value),property,parents));
        case GTE -> Specification.where(SQFilter.toGreaterEqualTo(((Long) value),property,parents));
        case LT -> Specification.where(SQFilter.toLess(((Long) value),property,parents));
        case LTE -> Specification.where(SQFilter.toLessEqualTo(((Long) value),property,parents));
        default ->
          Specification.where(negate ? SQFilter.toNotEquals(value, property, parents) : SQFilter.toEquals(value, property, parents));
      };
    } catch (JpaSystemException e) {
      throw new SQSpecificationException(e);
    }
  }
}
