package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;

import io.github.protasm.jvmud.engine.model.mudlib.EngineCapability;
import io.github.protasm.jvmud.language.compiler.type.LPCType;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Host clock, timezone, entropy and password hashing, plus compatibility control declarations.
 * Shutdown and runtime information retain their existing placeholder behavior.
 */
final class HostEfuns {
    private static final int PASSWORD_ITERATIONS = 210_000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_HASH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private HostEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_time", LPCType.LPCINT, List.of(),
                (runtime, args) -> (int) (System.currentTimeMillis() / 1000L)));
        efuns.add(of("jvmud_format_time", LPCType.LPCSTRING, List.of(LPCType.LPCINT),
                (runtime, args) -> formatTime(((Number) args[0]).longValue())));
        efuns.add(of("jvmud_random", LPCType.LPCINT, List.of(LPCType.LPCINT),
                (runtime, args) -> random(((Number) args[0]).intValue())));
        // Preserve this granted no-op; registering it does not provide a shutdown implementation.
        efuns.add(of("jvmud_shutdown", LPCType.LPCVOID, List.of(),
                (runtime, args) -> null)
                .requiring(EngineCapability.HOST_CONTROL));
        // The legacy runtime-information query retains its LPC-zero placeholder result.
        efuns.add(of("jvmud_runtime_info", LPCType.LPCMIXED, List.of(LPCType.LPCINT),
                (runtime, args) -> 0));
        efuns.add(of("jvmud_hash_password", LPCType.LPCSTRING, List.of(LPCType.LPCSTRING),
                (runtime, args) -> hashPassword(String.valueOf(args[0]))));
        efuns.add(of("jvmud_verify_password", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> verifyPassword(String.valueOf(args[0]), String.valueOf(args[1])) ? 1 : 0));
        return List.copyOf(efuns);
    }

    private static String hashPassword(String password) {
        byte[] salt = new byte[PASSWORD_SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, PASSWORD_ITERATIONS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "pbkdf2-sha256$" + PASSWORD_ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$"
                + encoder.encodeToString(hash);
    }

    private static boolean verifyPassword(String password, String encodedHash) {
        String[] parts = encodedHash.split("\\$");
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, PASSWORD_HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash password", e);
        }
    }

    private static String formatTime(long epochSeconds) {
        return DateTimeFormatter
                .ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.US)
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(epochSeconds));
    }

    private static int random(int max) {
        if (max <= 0) {
            return 0;
        }

        return ThreadLocalRandom.current().nextInt(max);
    }
}
