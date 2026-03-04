package com.card.iso8583.model;

public sealed interface FieldDefinition {

    int fieldNumber();
    String name();
    FieldType type();
    int maxLength();

    record Fixed(int fieldNumber, String name, FieldType type, int maxLength)
            implements FieldDefinition {}

    record LlVar(int fieldNumber, String name, FieldType type, int maxLength)
            implements FieldDefinition {}

    record LllVar(int fieldNumber, String name, FieldType type, int maxLength)
            implements FieldDefinition {}
}
