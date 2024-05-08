package com.eleodorodev.specification;

import jakarta.persistence.criteria.*;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * DynamicFilter Creator
 *
 * @author <a href="https://github.com/MatheusEleodoro">Matheus Eleodoro</a>
 * @version 1.0.0
 * @see <a href="https://github.com/MatheusEleodoro">...</a>
 */
public interface DynamicFilter {

    /**
     * DynamicSpecification toEquals
     *
     * @param compare   Object with the value to be compared
     * @param attribute name of the field to be compared
     * @param parents   optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<R>}
     * @apiNote Responsible for performing equality checks
     */
    static <R, T> DynamicSpecification<R> toEquals(T compare, String attribute, String... parents) {
        return (root, query, builder) -> {
            if (isNotEmpty(compare)) {
                Path<T> campoId;
                if (parents != null) {
                    Path<R> parentsPath = getPathRoot(root, parents);
                    campoId = parentsPath.get(attribute);
                } else {
                    campoId = root.get(attribute);
                }
                return builder.and(Collections.singletonList(builder.equal(campoId, compare)).toArray(new Predicate[0]));
            }
            return null;
        };
    }

    /**
     * DynamicSpecification toNotEquals
     *
     * @param compare   Object with the value to be compared
     * @param attribute name of the field to be compared
     * @param parents   optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<R>}
     * @apiNote Responsible for performing not equality checks
     */
    static <R, T> DynamicSpecification<R> toNotEquals(T compare, String attribute, String... parents) {
        return DynamicSpecification.not(toEquals(compare, attribute, parents));
    }


    /**
     * DynamicSpecification toLike
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the field to be compared
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for performing string containment checks
     */
    static <T> DynamicSpecification<T> toLike(Object compare, String attribute, String... parents) {
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
                return builder.and(Collections.singletonList(builder.like(builder
                                .lower(campoCompare), "%" + compare.toString().toLowerCase(Locale.ROOT) + "%"))
                        .toArray(new Predicate[0]));
            }
            return null;
        };
    }

    /**
     * DynamicSpecification toNotLike
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the field to be compared
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for performing string not containment checks
     */
    static <T> DynamicSpecification<T> toNotLike(Object compare, String attribute, String... parents) {
        return DynamicSpecification.not(toLike(compare, attribute, parents));
    }


    /**
     * DynamicSpecification toBetween
     *
     * @param start     - Initial object of the comparison
     * @param end       - Final object of the comparison
     * @param attribute - name of the attribute to be compared Ex.("date")
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return - {@link DynamicSpecification<R>}
     * @apiNote Responsible for checking if a value is within a range
     */
    static <T extends Comparable<T>, R> DynamicSpecification<R> toBetween(T start, T end, String attribute, String... parents) {
        return (root, query, builder) -> {
            if (isNotEmpty(start) && isNotEmpty(end)) {
                Path<T> attr;
                if (parents != null) {
                    Path<R> parentsPath = getPathRoot(root, parents);
                    attr = parentsPath.get(attribute);
                } else {
                    attr = root.get(attribute);
                }
                return builder.and(Collections.singletonList(builder.between(attr, start, end)).toArray(new Predicate[0]));
            }
            return null;
        };
    }

    /**
     * DynamicSpecification toNotBetween
     *
     * @param start     - Initial object of the comparison
     * @param end       - Final object of the comparison
     * @param attribute - name of the attribute to be compared Ex.("date")
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return - {@link DynamicSpecification<R>}
     * @apiNote Responsible for checking if a value is outside a range
     */
    static <T extends Comparable<T>, R> DynamicSpecification<R> toNotBetween(T start, T end, String attribute, String... parents) {
        return DynamicSpecification.not(toBetween(start, end, attribute, parents));
    }

    /**
     * DynamicSpecification toContains
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for validating if a value is within a list
     */
    static <T, R> DynamicSpecification<R> toContains(List<T> compare, String attribute, String... parents) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isNotEmpty(compare)) {
                Expression<T> campo;
                if (parents != null) {
                    Path<R> parentsPath = getPathRoot(root, parents);
                    campo = parentsPath.get(attribute);
                    predicates.add(campo.in(compare));
                } else {
                    campo = root.get(attribute);
                }
                predicates.add(campo.in(compare));
                return builder.and(predicates.toArray(new Predicate[0]));
            }
            return null;
        };
    }

    /**
     * DynamicSpecification toContains
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for validating if a value is outside a list
     */
    static <T, R> DynamicSpecification<R> toNotContains(List<T> compare, String attribute, String... parents) {
        return DynamicSpecification.not(toContains(compare, attribute, parents));
    }


    /**
     * DynamicSpecification toGreater
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for checking if one value is greater than another
     */
    static <T extends Comparable<T>, R> DynamicSpecification<R> toGreater(T compare, String attribute, String... parents) {
        return (root, query, builder) -> {
            if (isNotEmpty(compare)) {
                Path<T> campoId;
                if (parents != null) {
                    Path<R> parentsPath = getPathRoot(root, parents);
                    campoId = parentsPath.get(attribute);
                } else {
                    campoId = root.get(attribute);
                }
                return builder.and(Collections.singletonList(builder.greaterThan(campoId, compare)).toArray(new Predicate[0]));
            }
            return null;
        };
    }

    /**
     * DynamicSpecification toGreaterEqualTo
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for checking whether a value is greater than or equal to another
     */
    static <T extends Comparable<T>, R> DynamicSpecification<R> toGreaterEqualTo(T compare, String attribute, String... parents) {
        return (root, query, builder) -> {
            if (isNotEmpty(compare)) {
                Path<T> campoId;
                if (parents != null) {
                    Path<R> parentsPath = getPathRoot(root, parents);
                    campoId = parentsPath.get(attribute);
                } else {
                    campoId = root.get(attribute);
                }
                return builder.and(Collections.singletonList(builder.greaterThanOrEqualTo(campoId, compare)).toArray(new Predicate[0]));
            }
            return null;
        };
    }

    /**
     * DynamicSpecification toLess
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for checking if one value is less than another
     */
    static <T extends Comparable<T>, R> DynamicSpecification<R> toLess(T compare, String attribute, String... parents) {
        return (root, query, builder) -> {
            if (isNotEmpty(compare)) {
                Path<T> campoId;
                if (parents != null) {
                    Path<R> parentsPath = getPathRoot(root, parents);
                    campoId = parentsPath.get(attribute);
                } else {
                    campoId = root.get(attribute);
                }
                return builder.and(Collections.singletonList(builder.lessThan(campoId, compare)).toArray(new Predicate[0]));
            }
            return null;
        };
    }

    /**
     * DynamicSpecification toLessEqualTo
     *
     * @param compare   - Object with the value to be compared
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for checking whether a value is greater than or equal to another
     */
    static <T extends Comparable<T>, R> DynamicSpecification<R> toLessEqualTo(T compare, String attribute, String... parents) {
        return (root, query, builder) -> {
            if (isNotEmpty(compare)) {
                Path<T> campoId;
                if (parents != null) {
                    Path<R> parentsPath = getPathRoot(root, parents);
                    campoId = parentsPath.get(attribute);
                } else {
                    campoId = root.get(attribute);
                }
                return builder.and(Collections.singletonList(builder.lessThanOrEqualTo(campoId, compare)).toArray(new Predicate[0]));
            }
            return null;
        };
    }
    /**
     * DynamicSpecification toIsNull
     *
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for checking if value is null
     */
    static <T> DynamicSpecification<T> toIsNull(String attribute, String... parents) {
        return (root, query, builder) -> {
            Path<T> campoId;
            if (parents != null) {
                Path<T> parentsPath = getPathRoot(root, parents);
                campoId = parentsPath.get(attribute);
            } else {
                campoId = root.get(attribute);
            }
            return builder.and(Collections.singletonList(builder.isNull(campoId)).toArray(new Predicate[0]));
        };
    }

    /**
     * DynamicSpecification toIsNull
     *
     * @param attribute - name of the attribute to be queried
     * @param parents   - optional parameter that should be informed the parents where the attribute is nested
     * @return predicates - {@link DynamicSpecification<T>}
     * @apiNote Responsible for checking if value is not null
     */
    static <T> DynamicSpecification<T> toIsNotNull(String attribute, String... parents) {
        return (root, query, builder) -> {
            Path<T> campoId;
            if (parents != null) {
                Path<T> parentsPath = getPathRoot(root, parents);
                campoId = parentsPath.get(attribute);
            } else {
                campoId = root.get(attribute);
            }
            return builder.and(Collections.singletonList(builder.isNotNull(campoId)).toArray(new Predicate[0]));
        };
    }

    /**
     * Internal method for obtaining Path
     */
    static <T> Path<T> getPathRoot(Root<T> root, String... parents) {
        From<?, T> from = root;
        for (String parent : parents) {
            from = from.join(parent);
        }
        return from;
    }

    /**
     * Internal method to check if input is not null or empty
     */
    static boolean isNotEmpty(Object obj) {
        return !Objects.isNull(obj) && !obj.toString().isEmpty() &&
                (!(obj instanceof Number number) || number.intValue() != 0) &&
                (!(obj instanceof List<?>) || !((List<?>) obj).isEmpty());
    }

    /**
     * Internal method to Convert Object to List
     */
    @SuppressWarnings("unchecked")
    static <T> List<T> castList(Object obj) {
        return obj instanceof List<?> ? (List<T>) new ArrayList<>((List<?>) obj) : Collections.emptyList();
    }
}
