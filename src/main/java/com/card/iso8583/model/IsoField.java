package com.card.iso8583.model;

import java.util.Set;

public record IsoField(int fieldNumber, String value) {

    // Sensitive fields: PAN(2), Track2(35), Track1(45), PIN(52)
    private static final Set<Integer> SENSITIVE_FIELDS = Set.of(2, 35, 45, 52);

    @Override
    public String toString() {
        if (SENSITIVE_FIELDS.contains(fieldNumber)) {
            return "F%03d[%s]".formatted(fieldNumber, mask(value));
        }
        return "F%03d[%s]".formatted(fieldNumber, value);
    }

    private static String mask(String val) {
        if (val.length() <= 4) {
            return "****";
        }
        int visible = Math.min(4, val.length() - 4);
        return "*".repeat(val.length() - visible) + val.substring(val.length() - visible);
    }
}
