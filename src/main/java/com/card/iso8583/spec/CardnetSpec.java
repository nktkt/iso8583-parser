package com.card.iso8583.spec;

import com.card.iso8583.model.FieldDefinition;
import com.card.iso8583.model.FieldDefinition.*;
import com.card.iso8583.model.FieldType;

import static com.card.iso8583.model.FieldType.*;

/**
 * CARDNET-style ISO 8583 field definitions.
 * Based on standard ISO 8583:1987 field layout.
 */
public final class CardnetSpec {

    private CardnetSpec() {}

    private static final MessageSpec INSTANCE = buildSpec();

    public static MessageSpec getInstance() {
        return INSTANCE;
    }

    private static MessageSpec buildSpec() {
        return MessageSpec.builder()
                // Field 2: Primary Account Number (PAN)
                .add(new LlVar(2, "PAN", BCD_NUMERIC, 19))
                // Field 3: Processing Code
                .add(new Fixed(3, "Processing Code", BCD_NUMERIC, 6))
                // Field 4: Amount, Transaction
                .add(new Fixed(4, "Amount", BCD_AMOUNT, 12))
                // Field 5: Amount, Settlement
                .add(new Fixed(5, "Amount Settlement", BCD_AMOUNT, 12))
                // Field 7: Transmission Date & Time
                .add(new Fixed(7, "Transmission Date Time", BCD_NUMERIC, 10))
                // Field 9: Conversion Rate, Settlement
                .add(new Fixed(9, "Conversion Rate", BCD_NUMERIC, 8))
                // Field 10: Conversion Rate, Cardholder
                .add(new Fixed(10, "Conversion Rate Cardholder", BCD_NUMERIC, 8))
                // Field 11: System Trace Audit Number
                .add(new Fixed(11, "STAN", BCD_NUMERIC, 6))
                // Field 12: Time, Local Transaction
                .add(new Fixed(12, "Local Time", BCD_NUMERIC, 6))
                // Field 13: Date, Local Transaction
                .add(new Fixed(13, "Local Date", BCD_NUMERIC, 4))
                // Field 14: Date, Expiration
                .add(new Fixed(14, "Expiration Date", BCD_NUMERIC, 4))
                // Field 15: Date, Settlement
                .add(new Fixed(15, "Settlement Date", BCD_NUMERIC, 4))
                // Field 18: Merchant Category Code
                .add(new Fixed(18, "MCC", BCD_NUMERIC, 4))
                // Field 22: Point of Service Entry Mode
                .add(new Fixed(22, "POS Entry Mode", BCD_NUMERIC, 3))
                // Field 23: Card Sequence Number
                .add(new Fixed(23, "Card Sequence Number", BCD_NUMERIC, 3))
                // Field 25: Point of Service Condition Code
                .add(new Fixed(25, "POS Condition Code", BCD_NUMERIC, 2))
                // Field 26: POS PIN Capture Code
                .add(new Fixed(26, "POS PIN Capture Code", BCD_NUMERIC, 2))
                // Field 28: Amount, Transaction Fee
                .add(new Fixed(28, "Transaction Fee", ALPHA, 9))
                // Field 32: Acquiring Institution ID Code
                .add(new LlVar(32, "Acquiring Inst ID", BCD_NUMERIC, 11))
                // Field 33: Forwarding Institution ID Code
                .add(new LlVar(33, "Forwarding Inst ID", BCD_NUMERIC, 11))
                // Field 35: Track 2 Data
                .add(new LlVar(35, "Track 2 Data", ALPHA, 37))
                // Field 37: Retrieval Reference Number
                .add(new Fixed(37, "Retrieval Ref Number", ALPHA, 12))
                // Field 38: Authorization ID Response
                .add(new Fixed(38, "Auth ID Response", ALPHA, 6))
                // Field 39: Response Code
                .add(new Fixed(39, "Response Code", ALPHA, 2))
                // Field 40: Service Restriction Code
                .add(new Fixed(40, "Service Restriction Code", ALPHA, 3))
                // Field 41: Card Acceptor Terminal ID
                .add(new Fixed(41, "Terminal ID", ALPHA, 8))
                // Field 42: Card Acceptor ID Code
                .add(new Fixed(42, "Merchant ID", ALPHA, 15))
                // Field 43: Card Acceptor Name/Location
                .add(new Fixed(43, "Merchant Name", ALPHA, 40))
                // Field 44: Additional Response Data
                .add(new LlVar(44, "Additional Response Data", ALPHA, 25))
                // Field 45: Track 1 Data
                .add(new LlVar(45, "Track 1 Data", ALPHA, 76))
                // Field 48: Additional Data - Private
                .add(new LllVar(48, "Additional Data Private", ALPHA, 999))
                // Field 49: Currency Code, Transaction
                .add(new Fixed(49, "Currency Code", BCD_NUMERIC, 3))
                // Field 50: Currency Code, Settlement
                .add(new Fixed(50, "Currency Code Settlement", BCD_NUMERIC, 3))
                // Field 52: PIN Data
                .add(new Fixed(52, "PIN Data", BINARY, 8))
                // Field 53: Security Related Control Info
                .add(new Fixed(53, "Security Control Info", BCD_NUMERIC, 16))
                // Field 54: Additional Amounts
                .add(new LllVar(54, "Additional Amounts", ALPHA, 120))
                // Field 55: ICC System Related Data (EMV)
                .add(new LllVar(55, "EMV Data", BINARY, 999))
                // Field 60: Private Use
                .add(new LllVar(60, "Private Use", ALPHA, 60))
                // Field 61: Private Use
                .add(new LllVar(61, "Private Use 2", ALPHA, 99))
                // Field 62: Private Use
                .add(new LllVar(62, "Private Use 3", ALPHA, 999))
                // Field 63: Private Use
                .add(new LllVar(63, "Private Use 4", ALPHA, 999))
                // Field 70: Network Management Information Code
                .add(new Fixed(70, "Network Mgmt Info Code", BCD_NUMERIC, 3))
                // Field 90: Original Data Elements
                .add(new Fixed(90, "Original Data Elements", BCD_NUMERIC, 42))
                // Field 95: Replacement Amounts
                .add(new Fixed(95, "Replacement Amounts", ALPHA, 42))
                // Field 100: Receiving Institution ID Code
                .add(new LlVar(100, "Receiving Inst ID", BCD_NUMERIC, 11))
                // Field 102: Account ID 1
                .add(new LlVar(102, "Account ID 1", ALPHA, 28))
                // Field 103: Account ID 2
                .add(new LlVar(103, "Account ID 2", ALPHA, 28))
                // Field 120: Private Use
                .add(new LllVar(120, "Private Use 5", ALPHA, 999))
                // Field 123: POS Data Code
                .add(new LllVar(123, "POS Data Code", ALPHA, 999))
                // Field 125: Private Use
                .add(new LllVar(125, "Private Use 6", ALPHA, 999))
                // Field 126: Private Use
                .add(new LllVar(126, "Private Use 7", ALPHA, 999))
                // Field 127: Private Use
                .add(new LllVar(127, "Private Use 8", ALPHA, 999))
                // Field 128: MAC
                .add(new Fixed(128, "MAC", BINARY, 8))
                .build();
    }
}
