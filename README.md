# ISO 8583 Parser

A zero-dependency ISO 8583 message parser/builder written in pure Java 21, using `ByteBuffer` for binary encoding. No jPOS, no vendor MDK — just hand-crafted codec logic based on the CARDNET specification.

## Why

Most ISO 8583 libraries hide the wire format behind heavy abstractions. This project demonstrates a from-scratch understanding of the protocol: BCD encoding, bitmap manipulation, variable-length field framing, and TCP message transport — all with nothing but the JDK.

## Features

- **BCD codec** — pack/unpack numeric fields into Binary-Coded Decimal
- **Bitmap handling** — 64-bit and 128-bit (primary + secondary) bitmap support
- **Field types** — Fixed, LLVAR, LLLVAR with BCD numeric, alpha, binary, and amount types
- **Sealed classes + pattern matching** — exhaustive `switch` over field definitions, no `default` branch needed
- **TCP transport** — 2-byte big-endian length-prefixed framing with mock server for testing
- **Security hardened** — PAN/Track2/PIN masking in `toString()`, buffer bounds checking, length validation, socket timeouts

## Project Structure

```
src/main/java/com/card/iso8583/
├── model/          # IsoMessage, IsoField, FieldDefinition (sealed interface)
├── codec/          # BcdCodec, BitmapCodec, FieldCodec, IsoMessageCodec
├── spec/           # MessageSpec registry, CardnetSpec (field definitions)
├── tcp/            # LengthPrefixCodec, TcpClient, TcpServer
└── Main.java       # Demo entry point
```

## Requirements

- Java 21+
- Gradle 8+ (wrapper included)

## Quick Start

```bash
# Run tests
./gradlew test

# Run demo (builds and sends a sample 0100 Authorization Request)
./gradlew run
```

### Demo Output

```
=== ISO 8583 Parser Demo ===

Original message:
IsoMessage{mti=0100, F002[************0019], F003[000000], F004[000000001000], ...}

Packed (148 bytes):
01 00 72 3C 44 80 20 E0 80 00 16 47 61 34 00 00 ...

Unpacked message:
IsoMessage{mti=0100, F002[************0019], F003[000000], F004[000000001000], ...}

Round-trip match: true
```

## Message Types

| MTI  | Description              |
|------|--------------------------|
| 0100 | Authorization Request    |
| 0110 | Authorization Response   |
| 0200 | Financial Request        |
| 0210 | Financial Response       |

## Design

### Field Definitions (sealed interface + records)

```java
public sealed interface FieldDefinition {
    record Fixed(int fieldNumber, String name, FieldType type, int maxLength)
            implements FieldDefinition {}
    record LlVar(int fieldNumber, String name, FieldType type, int maxLength)
            implements FieldDefinition {}
    record LllVar(int fieldNumber, String name, FieldType type, int maxLength)
            implements FieldDefinition {}
}
```

The compiler enforces exhaustive matching — every variant must be handled:

```java
byte[] encoded = switch (def) {
    case Fixed f  -> encodeFixed(f, field);
    case LlVar f  -> encodeLlVar(f, field);
    case LllVar f -> encodeLllVar(f, field);
    // no default needed — all cases covered
};
```

### BCD Encoding

- MTI `"0100"` → `0x01 0x00` (2 bytes)
- PAN `"4761340000000019"` → 8 bytes packed BCD
- Odd-length digits are left-zero-padded before packing

### Bitmap

- Primary bitmap: 8 bytes (fields 2–64)
- Bit 1 = secondary bitmap present flag
- Secondary bitmap: additional 8 bytes (fields 65–128)

### TCP Framing

```
[2-byte BE length] [ISO 8583 message bytes]
```

## Tests

```
42 tests covering:
  - BCD encode/decode round-trips
  - Bitmap build/parse with primary and secondary
  - Field codec for all types (Fixed, LLVAR, LLLVAR × BCD/Alpha/Binary)
  - Full message pack/unpack round-trips
  - Sample messages (0100, 0110, 0200)
  - TCP client-server round-trip
  - Input validation (non-digit rejection, invalid field numbers)
```

## Security

- Sensitive fields (PAN, Track 2, Track 1, PIN) are masked in `toString()` output
- LLVAR/LLLVAR decoded lengths are validated against `maxLength`
- Buffer remaining bytes are checked before every read
- TCP messages are capped at 8 KB (`MAX_MESSAGE_SIZE`)
- Socket read timeouts (30s) prevent slowloris-style DoS
- BCD encoder rejects non-digit characters

**Not included (required for production):** TLS encryption, mutual authentication, connection rate limiting, HSM integration for PIN blocks.

## Tech Stack

- Java 21 (records, sealed classes, pattern matching, virtual threads)
- Gradle Kotlin DSL
- JUnit 5 (test-only dependency)
- Zero runtime dependencies

## License

MIT
