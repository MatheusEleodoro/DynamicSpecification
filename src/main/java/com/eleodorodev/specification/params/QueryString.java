package com.eleodorodev.specification.params;


import com.eleodorodev.specification.enums.Conditional;
import com.eleodorodev.specification.params.deserialize.ListDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

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
    private boolean search;

    public <T> T toObj(Class<T> type) {
        Map<String, Object> map = this.value()
            .entrySet().stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().getFirst()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, new ListDeserializer());
        return mapper.convertValue(map, type);
    }

    public static QueryString instance() {
        return new QueryString(new HashMap<>());
    }

    public QueryString withParams(String paramName, Object value, Conditional conditional) {
        this.value.put(paramName, Pair.of(value, conditional.name()));
        return this;
    }

    public QueryString withParams(String paramName, Object value) {
        this.value.put(paramName, Pair.of(value, ""));
        return this;
    }
}

