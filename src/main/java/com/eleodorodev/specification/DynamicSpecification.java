package com.eleodorodev.specification;


import com.eleodorodev.specification.annotation.DynamicSpecAttr;
import com.eleodorodev.specification.enums.Conditional;
import com.eleodorodev.specification.enums.Conjunction;
import com.eleodorodev.specification.exception.DynamicSpecificationException;
import com.eleodorodev.specification.params.DynamicArgs;
import com.eleodorodev.specification.params.DynamicArgsConverter;
import com.eleodorodev.specification.params.deserialize.ListDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SQSpecification
 * @author matheus.souza
 * @version 1.0.2
 * @apiNote A customized implementation of the {@link Specification} with it is possible to create queries SQally, either from the body or using querystring
 * @see <a href="https://github.com/MatheusEleodoro">...</a>
 */
public interface DynamicSpecification<T> extends Specification<T> {


    /**
     * Bind
     * Links the query string received in the request url, and builds a query specification
     *
     * @param clazz     Class with the annotation {@link DynamicSpecAttr}
     * @param DynamicArgs Object {@link DynamicArgs} Received by {@link RequestParam @RequestParam QueryString}
     * @return {@link Specification}
     */
    static <R, T> Specification<R> bind(Class<T> clazz, DynamicArgs DynamicArgs) throws DynamicSpecificationException {
        try {
            AtomicReference<Specification<R>> spec = new AtomicReference<>();
            AtomicBoolean first = new AtomicBoolean(true);


            DynamicArgs dynamicArgsLocal = Objects.requireNonNullElse(DynamicArgs, new DynamicArgs(new HashMap<>()));

            var values = Stream.of(clazz.getDeclaredFields())
                .flatMap(field -> AnnotatedElementUtils.findAllMergedAnnotations(field, DynamicSpecAttr.class).stream()
                    .map(annotation -> Map.entry(field, annotation)))
                .filter(n -> dynamicArgsLocal.value().containsKey(n.getValue().property()) || dynamicArgsLocal.value().containsKey(n.getValue().alias()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            values.forEach((key, specAttr) -> {
                Conditional conditional = specAttr.conditional();
                Conjunction conjunction = specAttr.conjunction();

                Pair<Object, String> params = Binder.getArgsValues(key, specAttr, dynamicArgsLocal);

                Object value = params.getFirst();
                boolean negate = specAttr.negate();

                if (dynamicArgsLocal.search() && !params.getSecond().isEmpty()) {
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
                    spec.set(apply(spec.get(), conditional, null, specAttr, value, negate));
                    first.set(false);
                    return;
                }
                spec.getAndSet(apply(spec.get(), conditional, conjunction, specAttr, value, negate));

            });
            return spec.get();
        } catch (JpaSystemException e) {
            throw new DynamicSpecificationException("Failed to generate Specification queries", e);
        }
    }

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
                case GT ->
                    spec.and(DynamicFilter.toGreater(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case GTE ->
                    spec.and(DynamicFilter.toGreaterEqualTo(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case LT ->
                    spec.and(DynamicFilter.toLess(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case LTE ->
                    spec.and(DynamicFilter.toLessEqualTo(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                default ->
                    spec.and(negate ? DynamicFilter.toNotEquals(value, property, parents) : DynamicFilter.toEquals(value, property, parents));
            };
        } catch (RuntimeException e) {
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
                    spec.or(DynamicFilter.toGreater(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case GTE ->
                    spec.or(DynamicFilter.toGreaterEqualTo(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case LT ->
                    spec.or(DynamicFilter.toLess(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case LTE ->
                    spec.or(DynamicFilter.toLessEqualTo(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                default ->
                    spec.or(negate ? DynamicFilter.toNotEquals(value, property, parents) : DynamicFilter.toEquals(value, property, parents));
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
                    Specification.where(DynamicFilter.toGreater(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case GTE ->
                    Specification.where(DynamicFilter.toGreaterEqualTo(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case LT ->
                    Specification.where(DynamicFilter.toLess(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                case LTE ->
                    Specification.where(DynamicFilter.toLessEqualTo(DynamicArgsConverter.parseNumber(value.toString()), property, parents));
                default ->
                    Specification.where(negate ? DynamicFilter.toNotEquals(value, property, parents) : DynamicFilter.toEquals(value, property, parents));
            };
        } catch (JpaSystemException e) {
            throw new DynamicSpecificationException(e);
        }
    }

    /**
     * Used to deny queries - INTERNAL
     */
    static <T> DynamicSpecification<T> not(@Nullable Specification<T> spec) {
        return spec == null ? (root, query, builder) -> null : (root, query, builder) -> builder.not(spec.toPredicate(root, query, builder));
    }

    static <T> Specification<T> where(@NonNull Specification<T> spec) {
        return Specification.where(spec);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class Binder {

        static Pair<Object, String> getArgsValues(Field key ,DynamicSpecAttr specAttr,  DynamicArgs args) {
            Pair<Object, String> params = args.value().getOrDefault(specAttr.property(), args.value().get(specAttr.alias()));
            JsonDeserialize deserialize = key.getAnnotation(JsonDeserialize.class);
            if (deserialize != null && deserialize.using().equals(ListDeserializer.class) &&
                !(params.getFirst() instanceof List))
                params = Pair.of(List.of(params.getFirst()), params.getSecond());

            return params;
        }

    }
}
