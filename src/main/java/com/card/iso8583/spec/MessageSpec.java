package com.card.iso8583.spec;

import com.card.iso8583.model.FieldDefinition;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class MessageSpec {

    private final Map<Integer, FieldDefinition> fields;

    private MessageSpec(Map<Integer, FieldDefinition> fields) {
        this.fields = Collections.unmodifiableMap(new TreeMap<>(fields));
    }

    public FieldDefinition getField(int fieldNumber) {
        return fields.get(fieldNumber);
    }

    public Map<Integer, FieldDefinition> allFields() {
        return fields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<Integer, FieldDefinition> fields = new TreeMap<>();

        public Builder add(FieldDefinition def) {
            fields.put(def.fieldNumber(), def);
            return this;
        }

        public MessageSpec build() {
            return new MessageSpec(fields);
        }
    }
}
