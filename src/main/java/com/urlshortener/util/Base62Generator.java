package com.urlshortener.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Utility for generating cryptographically-random Base62 short codes.
 *
 * <p><strong>Base62 alphabet:</strong> a–z + A–Z + 0–9 = 62 characters.
 *
 * <p><strong>Capacity:</strong> With a 6-char code, the total possible
 * combinations are 62^6 = 56,800,235,584 (over 56 billion) — enough for
 * any practical use case while keeping URLs short.
 *
 * <p>Uses {@link SecureRandom} (a CSPRNG) instead of {@link java.util.Random}
 * to make codes unpredictable and resistant to enumeration attacks.
 */
@Component
public class Base62Generator {

    private static final String ALPHABET =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int ALPHABET_SIZE = ALPHABET.length(); // 62

    /**
     * SecureRandom is thread-safe and cryptographically strong.
     * It is initialised once and reused for all calls.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random Base62 string of the given length.
     *
     * @param length Number of characters in the output code
     * @return Random Base62 string (e.g., "aB3xZ9" for length=6)
     */
    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET_SIZE)));
        }
        return sb.toString();
    }
}
