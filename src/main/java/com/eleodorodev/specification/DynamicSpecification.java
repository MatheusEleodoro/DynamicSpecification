package com.eleodorodev.specification;


import com.eleodorodev.specification.params.QueryString;
import com.eleodorodev.specification.params.QueryStringConverter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.JpaSystemException;
import com.eleodorodev.specification.annotations.DynamicSpecAttr;
import com.eleodorodev.specification.enums.Conditional;
import com.eleodorodev.specification.enums.Conjunction;
import com.eleodorodev.specification.exception.DynamicSpecificationException;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DynamicSpecification
 *
 * @author <a href="https://github.com/MatheusEleodoro">Matheus Eleodoro</a>
 * @version 1.0.0
 * @apiNote A customized implementation of the {@link Specification} with it is possible to create queries dynamically, either from the body or using querystring
 * @see <a href="https://github.com/MatheusEleodoro">...</a>
 */
public interface DynamicSpecification<T> extends Specification<T> {

    /**
     * Bind
     * Links the query string received in the request url, and builds a query specification
     *
     * @param clazz       Class with the annotation {@link DynamicSpecAttr}
     * @param querystring Object {@link QueryString} Received by {@link RequestParam @RequestParam QueryString}
     * @return {@link Specification}
     */
    static <R, T> Specification<R> bind(Class<T> clazz, QueryString querystring) throws DynamicSpecificationException {
        try {
            AtomicReference<Specification<R>> spec = new AtomicReference<>();
            AtomicBoolean first = new AtomicBoolean(true);
            var values = Stream.of(clazz.getDeclaredFields())
                    .flatMap(field -> AnnotatedElementUtils.findAllMergedAnnotations(field, DynamicSpecAttr.class).stream()
                            .map(annotation -> Map.entry(field, annotation)))
                    .filter(n -> querystring.value().containsKey(n.getValue().property()) || querystring.value().containsKey(n.getValue().alias()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            values.forEach((key, specAttr) -> {
                Conditional conditional = specAttr.conditional();
                Conjunction conjunction = specAttr.conjunction();
                Pair<Object, String> params = querystring.value()
                        .getOrDefault(specAttr.property(), querystring.value().get(specAttr.alias()));

                Object value = params.getFirst();
                boolean negate = specAttr.negate();
                if (querystring.searchURL() && !params.getSecond().isEmpty()) {
                    List<String> args = new ArrayList<>(Arrays.asList(params.getSecond().split(",")));
                    List<String> validArgs = new ArrayList<>(Arrays.stream(Conjunction.values()).map(Enum::name).toList());
                    validArgs.addAll(Arrays.stream(Conditional.values()).map(Enum::name).toList());

                    args = args.stream().map(String::toUpperCase).toList();
                    args = args.stream().filter(validArgs::contains).collect(Collectors.toList());

                    if (args.size() == 1) {
                        args.addFirst("AND");
                    }

                    args.sort(Comparator.comparing((String s) -> !s.equalsIgnoreCase("AND") && !s.equalsIgnoreCase("OR"))
                            .thenComparing((String s) -> s.equalsIgnoreCase("NOT") ? 2 : 1));


                    conjunction = Conjunction.valueOf(args.getFirst());
                    conditional = Conditional.valueOf(args.get(1));

                    negate = args.contains("NOT");
                }
                if (first.get()) {
                    spec.set(DynamicSpecificationBuilder.apply(spec.get(), conditional, null, specAttr, value, negate));
                    first.set(false);
                    return;
                }
                spec.getAndSet(DynamicSpecificationBuilder.apply(spec.get(), conditional, conjunction, specAttr, value, negate));

            });
            return spec.get();
        } catch (JpaSystemException e) {
            throw new DynamicSpecificationException("Failed to generate Specification queries", e);
        }
    }


    /**
     * Used to deny queries - INTERNAL
     */
    static <T> DynamicSpecification<T> not(@Nullable Specification<T> spec) {
        return spec == null ? (root, query, builder) -> null : (root, query, builder) -> builder.not(spec.toPredicate(root, query, builder));
    }

    static <T> Specification<T> where(@Nullable Specification<T> spec) {
        return Specification.where(spec);
    }


   class DynamicSpecificationBuilder {

        /**
         * Apply the binds - INTERNAL
         */
        static <T> Specification<T> apply(Specification<T> spec, Conditional conditional, Conjunction conjunction, DynamicSpecAttr specAttr, Object value, boolean negate) throws DynamicSpecificationException {
            try {
                String property = specAttr.property();
                String[] parents = specAttr.parents().length == 0 ? null : specAttr.parents();

                if (conjunction == null) return execWhere(negate, value, property, parents, conditional);

                return switch (conjunction) {
                    case OR -> execOr(negate, value, property, parents, conditional, spec);
                    case AND -> execAnd(negate, value, property, parents, conditional, spec);
                };
            } catch (JpaSystemException e) {
                throw new DynamicSpecificationException(e);
            }
        }

        /**
         * Create WHERE - INTERNAL
         */
        static <T> Specification<T> execWhere(boolean negate, Object value, String property, String[] parents,
                                       Conditional conditional) throws DynamicSpecificationException {
            try {
                List<Long> element = DynamicFilter.castList(value);
                return switch (conditional) {
                    case LK ->
                            Specification.where(negate ? DynamicFilter.toNotLike(value, property, parents) : DynamicFilter.toLike(value, property, parents));
                    case CT ->
                            Specification.where(negate ? DynamicFilter.toNotContains(((List<?>) value), property, parents) : DynamicFilter.toContains(((List<?>) value), property, parents));
                    case BW ->
                            Specification.where(negate ? DynamicFilter.toNotBetween(element.getFirst(), element.get(1), property, parents) : DynamicFilter.toBetween(element.getFirst(), element.get(1), property, parents));
                    case GT ->
                            Specification.where(DynamicFilter.toGreater(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    case GTE ->
                            Specification.where(DynamicFilter.toGreaterEqualTo(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    case LT ->
                            Specification.where(DynamicFilter.toLess(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    case LTE ->
                            Specification.where(DynamicFilter.toLessEqualTo(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    default ->
                            Specification.where(negate ? DynamicFilter.toNotEquals(value, property, parents) : DynamicFilter.toEquals(value, property, parents));
                };
            } catch (JpaSystemException e) {
                throw new DynamicSpecificationException(e);
            }
        }


        /**
         * Create OR - INTERNAL
         */
        static <T> Specification<T> execOr(boolean negate, Object value, String property, String[] parents,
                                    Conditional conditional, Specification<T> spec) throws DynamicSpecificationException {

            try {
                List<Long> element = DynamicFilter.castList(value);
                return switch (conditional) {
                    case LK ->
                            spec.or(negate ? DynamicFilter.toNotLike(value, property, parents) : DynamicFilter.toLike(value, property, parents));
                    case CT ->
                            spec.or(negate ? DynamicFilter.toNotContains(DynamicFilter.castList(value), property, parents) : DynamicFilter.toContains(DynamicFilter.castList(value), property, parents));
                    case BW ->
                            spec.or(negate ? DynamicFilter.toNotBetween(element.getFirst(), element.get(1), property, parents) : DynamicFilter.toBetween(element.getFirst(), element.get(1), property, parents));
                    case GT ->
                            spec.or(DynamicFilter.toGreater(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    case GTE ->
                            spec.or(DynamicFilter.toGreaterEqualTo(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    case LT ->
                            spec.or(DynamicFilter.toLess(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    case LTE ->
                            spec.or(DynamicFilter.toLessEqualTo(QueryStringConverter.parseNumber(value.toString()), property, parents));
                    default ->
                            spec.or(negate ? DynamicFilter.toNotEquals(value, property, parents) : DynamicFilter.toEquals(value, property, parents));
                };
            } catch (JpaSystemException e) {
                throw new DynamicSpecificationException(e);
            }
        }

        /**
         * Create AND - INTERNAL
         */
        static <T> Specification<T> execAnd(boolean negate, Object value, String property, String[] parents,
                                     Conditional conditional, Specification<T> spec) throws DynamicSpecificationException {
            try {
                List<Long> element = DynamicFilter.castList(value);
                return switch (conditional) {
                    case LK ->
                            spec.and(negate ? DynamicFilter.toNotLike(value, property, parents) : DynamicFilter.toLike(value, property, parents));
                    case CT ->
                            spec.and(negate ? DynamicFilter.toNotContains(DynamicFilter.castList(value), property, parents) : DynamicFilter.toContains(DynamicFilter.castList(value), property, parents));
                    case BW ->
                            spec.and(negate ? DynamicFilter.toNotBetween(element.getFirst(), element.get(1), property, parents) : DynamicFilter.toBetween(element.getFirst(), element.get(1), property, parents));
                    case GT -> spec.and(DynamicFilter.toGreater(((Long) value), property, parents));
                    case GTE -> spec.and(DynamicFilter.toGreaterEqualTo(((Long) value), property, parents));
                    case LT -> spec.and(DynamicFilter.toLess(((Long) value), property, parents));
                    case LTE -> spec.and(DynamicFilter.toLessEqualTo(((Long) value), property, parents));
                    default ->
                            spec.and(negate ? DynamicFilter.toNotEquals(value, property, parents) : DynamicFilter.toEquals(value, property, parents));
                };
            } catch (RuntimeException e) {
                throw new DynamicSpecificationException(e);
            }
        }
    }
}
