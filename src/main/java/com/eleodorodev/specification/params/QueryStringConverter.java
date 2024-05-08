package com.eleodorodev.specification.params;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@ConfigurationPropertiesBinding
public class QueryStringConverter implements Converter<String, QueryString> {
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
