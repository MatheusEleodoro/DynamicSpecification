package com.eleodorodev.specification.params.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ListDeserializer extends JsonDeserializer<List<?>> {


    @Override
    public List<?> deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        JsonNode nodes = parser.getCodec().readTree(parser);
        List<Object> values = new ArrayList<>();
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();

        for (JsonNode node : nodes) {
            values.add(mapper.treeToValue(node, Object.class));
        }

        if (nodes.isValueNode() && values.isEmpty()) {
            values.add(mapper.treeToValue(nodes, Object.class));
        }

        return values;
    }
}
