package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.persistence.filesystem.LpmuseumAccountFileStore;
import io.github.protasm.jvmud.persistence.filesystem.LpmuseumAccountFileStore.Account;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** LPMuseum-specific durable account and password policy, isolated from the generic instance host. */
final class LpmuseumAccountService {
    private static final int PASSWORD_ITERATIONS = 210_000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_HASH_BITS = 256;

    private final Path mudlibRoot;
    private final LpmuseumAccountFileStore store = new LpmuseumAccountFileStore();
    private final SecureRandom random = new SecureRandom();

    /** Creates account policy rooted in the selected LPMuseum mudlib. */
    LpmuseumAccountService(Path mudlibRoot) {
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
    }

    /** Loads an account by normalized account id. */
    Optional<Account> load(String accountId) {
        return store.load(mudlibRoot, accountId);
    }

    /** Saves an account through the LPMuseum filesystem adapter. */
    void save(Account account) {
        store.save(mudlibRoot, account);
    }

    /** Produces a salted PBKDF2 password representation. */
    String hashPassword(String password) {
        byte[] salt = new byte[PASSWORD_SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, PASSWORD_ITERATIONS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "pbkdf2-sha256$" + PASSWORD_ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
    }

    /** Verifies a candidate password against a stored PBKDF2 representation. */
    boolean verifyPassword(String password, String encodedHash) {
        String[] parts = encodedHash.split("\\$");
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, pbkdf2(password, salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, PASSWORD_HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash password", e);
        }
    }
}
