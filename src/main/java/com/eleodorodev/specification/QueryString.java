package com.eleodorodev.specification;


import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

/**
 * QueryString
 * @apiNote Object that must be received in the Controller Annotated as  {@link RequestParam @Request param} to receive querystring
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class QueryString {
    @NonNull
    private Map<String, Pair<Object, String>> value;
    /**
     * Enables or disables permission to make complete queries via URL, for example allowing the user to accept parameters AND, OR, EQ, BT, etc.
     */
    @Setter
    private boolean searchURL;
}

@Component
@ConfigurationPropertiesBinding
class QueryStringConverter implements Converter<String, QueryString> {
    @Override
    @SneakyThrows
    public QueryString convert(@NonNull String value) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest();


        Map<String, String[]> map = request.getParameterMap();

        String firstKey = map.keySet().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("URL Map is empty"));

        var listMaps = Arrays.stream(map.get(firstKey)).map(e -> Map.of(e.split("=")[0], e.split("=")[1]))
                .toList();

        Map<String, Pair<Object, String>> params = new HashMap<>();
        listMaps.forEach(mp -> mp.forEach((key, v) -> params.put(key, getPair(v.split(";")))));

        map.forEach((k, v) -> {
            if (!Objects.equals(k, firstKey)) {
                params.put(k, getPair(v[0].split(";")));
            }

        });
        return new QueryString(params);
    }

    private static Pair<Object, String> getPair(String[] param) {
        return Pair.of(param[0].contains(",") ? Stream.of(param[0].split(","))
                        .map(QueryStringConverter::parseNumber).toList() : parseNumber(param[0]),
                param.length > 1 ? param[1].replace(">", "") : "");
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseNumber(String str) {
        try {
            if (!str.matches("\\d*\\.?\\d+")) throw new ParseException("", 0);
            return ((T) NumberFormat.getNumberInstance().parse(str));
        } catch (ParseException e) {
            return ((T) str);
        }
    }
}
