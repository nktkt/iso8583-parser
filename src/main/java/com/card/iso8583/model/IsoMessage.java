package com.card.iso8583.model;

import java.util.Collections;
import java.util.TreeMap;

public record IsoMessage(String mti, TreeMap<Integer, IsoField> fields) {

    public IsoMessage {
        if (mti == null || mti.length() != 4) {
            throw new IllegalArgumentException("MTI must be 4 digits: " + mti);
        }
        fields = new TreeMap<>(fields);
    }

    public static Builder builder(String mti) {
        return new Builder(mti);
    }

    public IsoField getField(int fieldNumber) {
        return fields.get(fieldNumber);
    }

    public boolean hasField(int fieldNumber) {
        return fields.containsKey(fieldNumber);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("IsoMessage{mti=").append(mti);
        fields.forEach((k, v) -> sb.append(", ").append(v));
        return sb.append('}').toString();
    }

    public static final class Builder {
        private final String mti;
        private final TreeMap<Integer, IsoField> fields = new TreeMap<>();

        private Builder(String mti) {
            this.mti = mti;
        }

        public Builder setField(int fieldNumber, String value) {
            fields.put(fieldNumber, new IsoField(fieldNumber, value));
            return this;
        }

        public IsoMessage build() {
            return new IsoMessage(mti, fields);
        }
    }
}
