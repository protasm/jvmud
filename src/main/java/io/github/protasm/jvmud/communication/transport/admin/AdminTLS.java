package io.github.protasm.jvmud.communication.transport.admin;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.cert.*;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

/** Persistent TLS server identity and explicit certificate pinning for administration consoles. */
public final class AdminTLS {
    private AdminTLS() {}

    /** Creates a local self-signed identity once, then loads it; administrators distribute its fingerprint securely. */
    public static SSLContext server(Path directory) throws Exception {
        Path store = directory.resolve("admin-tls.p12");
        // The keystore is protected by filesystem permissions; its fixed password is not an authentication secret.
        char[] password = "jvmud-local-keystore".toCharArray();
        if (!Files.exists(store)) {
            Path temporary = Files.createTempDirectory(directory, ".tls-");
            Path generated = temporary.resolve("identity.p12");
            try {
                Process keytool = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                        "-genkeypair", "-alias", "admin", "-keyalg", "EC", "-groupname", "secp256r1", "-validity", "3650",
                        "-dname", "CN=JVMud Administration", "-storetype", "PKCS12", "-keystore", generated.toString(),
                        "-storepass", new String(password), "-noprompt").redirectErrorStream(true)
                        .redirectOutput(temporary.resolve("keytool.log").toFile()).start();
                if (!keytool.waitFor(30, TimeUnit.SECONDS)) { keytool.destroyForcibly(); throw new IOException("TLS identity generation timed out."); }
                if (keytool.exitValue() != 0) throw new IOException("TLS identity generation failed: " + Files.readString(temporary.resolve("keytool.log")));
                Files.setPosixFilePermissions(generated, PosixFilePermissions.fromString("rw-------"));
                Files.move(generated, store, StandardCopyOption.ATOMIC_MOVE);
            } finally {
                try (var files = Files.list(temporary)) { for (Path file : files.toList()) Files.deleteIfExists(file); }
                Files.deleteIfExists(temporary);
            }
        }
        KeyStore keys = KeyStore.getInstance("PKCS12");
        try (var input = Files.newInputStream(store)) { keys.load(input, password); }
        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keys, password);
        SSLContext context = SSLContext.getInstance("TLS"); context.init(factory.getKeyManagers(), null, null);
        Files.writeString(directory.resolve("admin-tls.sha256"), fingerprint(keys.getCertificate("admin")) + "\n");
        return context;
    }

    /** Trusts exactly the operator-supplied certificate fingerprint; never learns trust from the network. */
    public static SSLContext client(String fingerprint) throws GeneralSecurityException {
        String expected = fingerprint.replace(":", "").toLowerCase(java.util.Locale.ROOT);
        if (!expected.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("A SHA-256 certificate fingerprint is required.");
        X509TrustManager trust = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { throw new CertificateException("Client certificates are not used."); }
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (chain.length == 0) throw new CertificateException("No server certificate.");
                chain[0].checkValidity();
                try {
                    if (!MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                            fingerprint(chain[0]).getBytes(java.nio.charset.StandardCharsets.US_ASCII)))
                        throw new CertificateException("Administration certificate fingerprint does not match.");
                } catch (GeneralSecurityException e) { throw new CertificateException(e); }
            }
        };
        SSLContext context = SSLContext.getInstance("TLS"); context.init(null, new TrustManager[]{trust}, null); return context;
    }

    /** SHA-256 pin for an exact certificate, suitable for out-of-band distribution. */
    public static String fingerprint(java.security.cert.Certificate certificate) throws GeneralSecurityException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()));
    }
}
