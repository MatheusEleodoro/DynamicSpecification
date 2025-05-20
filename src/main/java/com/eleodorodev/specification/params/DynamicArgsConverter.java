package com.eleodorodev.specification.params;

import com.eleodorodev.specification.exception.DynamicParamArgumentException;
import com.eleodorodev.specification.params.annotation.DynamicParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class QueryStringConverter
 *
 * @apiNote This class is responsible for converting HTTP request parameters into a map of key-value pairs.
 * @author Matheus Eleodoro
 * @see <a href="https://github.com/MatheusEleodoro">GitHub Profile</a>
 */
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicArgsConverter {

    /**
     * Parses a string into a number (Double, BigDecimal, or returns the original string if parsing fails).
     *
     * @param str The string to parse.
     * @param <T> The type of the parsed number.
     * @return The parsed number or the original string if parsing fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseNumber(String str) {
        try {
            if (!str.matches("\\d*\\.?\\d+")) throw new ParseException("", 0);
            Number number = NumberFormat.getNumberInstance().parse(str);
            if (number instanceof Double n && n.byteValue() < 0) {
                return (T) new BigDecimal(str);
            } else {
                return (T) number;
            }
        } catch (ParseException e) {
            return ((T) str);
        }
    }

    /**
     * Converts the parameters of an HTTP request into a map of key-value pairs.
     *
     * @param request    The HTTP request containing the parameters.
     * @param annotation The annotation containing additional configuration.
     * @return A map where the key is the parameter name and the value is a pair of the parameter value and its type.
     * @throws IllegalArgumentException If the URL parameter map is empty.
     */
    public static DynamicArgs converter(HttpServletRequest request, DynamicParam annotation) {
        String paramName = annotation.name();
        String query = request.getQueryString();

        if (query == null || !query.contains(paramName + "=")) {
            if (!annotation.required()) return DynamicArgs.instance();
            throw new DynamicParamArgumentException("Expected QueryArg parameter '" + paramName + "' not found.");
        }

        int index = query.indexOf(paramName + "=");
        String paramString = query.substring(index + paramName.length() + 1);

        try {
            paramString = URLDecoder.decode(paramString, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode query string.", e);
        }

        Map<String, Pair<Object, String>> params = Arrays.stream(paramString.split("&"))
                .filter(pair -> !pair.isBlank())
                .map(pair -> pair.split("=", 2))
                .filter(kv -> kv.length > 1 && !kv[1].isBlank())
                .collect(Collectors.toMap(
                        kv -> kv[0],
                        kv -> getPair(kv[1].split(";")),
                        (existing, replacement) -> replacement,
                        HashMap::new
                ));

        if (annotation.pageable()) {
            PublicResolverNames.paramsNames().forEach(params::remove);
        }

        return new DynamicArgs(params)
                .search(annotation.search())
                .type(annotation.type());
    }

    /**
     * Creates a pair object from a parameter array.
     *
     * @param param The parameter array where the first element is the value and the second (optional) is the type.
     * @return A pair containing the parsed value and its type.
     */
    private static Pair<Object, String> getPair(String[] param) {
        return Pair.of(param[0].contains(",") ? Stream.of(param[0].split(","))
                .map(DynamicArgsConverter::parseNumber).toList() : parseNumber(param[0]),
            param.length > 1 ? param[1].replace(">", "") : "");
    }

    /**
     * Inner class PublicResolverNames
     *
     * @apiNote Provides utility methods to retrieve parameter names for pagination and sorting.
     */
    protected static class PublicResolverNames extends PageableHandlerMethodArgumentResolver {

        /**
         * Retrieves the name of the page parameter.
         *
         * @return The page parameter name.
         */
        public String getPageParam() {
            return super.getPageParameterName();
        }

        /**
         * Retrieves the name of the size parameter.
         *
         * @return The size parameter name.
         */
        public String getSizeParam() {
            return super.getSizeParameterName();
        }

        /**
         * Retrieves the name of the sort parameter.
         *
         * @return The sort parameter name.
         */
        public String getSortParam() {
            return "sort";
        }

        /**
         * Retrieves a list of parameter names used for pagination and sorting.
         *
         * @return A list of parameter names.
         */
        public static List<String> paramsNames() {
            PublicResolverNames resolver = new PublicResolverNames();
            return List.of(
                resolver.getPageParam(),
                resolver.getSizeParam(),
                resolver.getSortParam()
            );
        }
    }
}