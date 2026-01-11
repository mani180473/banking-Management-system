// src/main/java/util/PasswordUtils.java
package util;

import org.mindrot.jbcrypt.BCrypt;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class PasswordUtils {

    // Generate a bcrypt hash for a plaintext password
    public static String hashBcrypt(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    // Check whether a stored password looks like a bcrypt hash
    public static boolean isBcrypt(String stored) {
        return stored != null && stored.startsWith("$2");
    }

    // Check whether a stored password looks like a SHA-256 hex string
    public static boolean isSha256Hex(String stored) {
        return stored != null && stored.matches("^[0-9a-fA-F]{64}$");
    }

    // Compute SHA-256 hex of the given plaintext
    public static String sha256Hex(String plain) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Verify a plaintext password against stored value supporting bcrypt and legacy SHA-256/plain
    public static boolean verify(String plain, String stored) {
        try {
            if (stored == null) return false;
            if (isBcrypt(stored)) {
                return BCrypt.checkpw(plain, stored);
            } else if (isSha256Hex(stored)) {
                return sha256Hex(plain).equalsIgnoreCase(stored);
            } else {
                // legacy plaintext comparison (rare) - allow, but should be migrated
                return plain.equals(stored);
            }
        } catch (Exception e) {
            return false;
        }
    }
}
