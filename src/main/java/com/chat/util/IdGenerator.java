package com.chat.util;

import java.util.UUID;

public class IdGenerator {
    
    public static String generateNumericId() {
        UUID uuid = UUID.randomUUID();
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        return String.valueOf(Math.abs(mostSigBits)) + String.valueOf(Math.abs(leastSigBits));
    }
    
    public static String generateShortId() {
        UUID uuid = UUID.randomUUID();
        long mostSigBits = uuid.getMostSignificantBits();
        return String.valueOf(Math.abs(mostSigBits));
    }
}
