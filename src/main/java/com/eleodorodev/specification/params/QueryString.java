package com.eleodorodev.specification.params;


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

