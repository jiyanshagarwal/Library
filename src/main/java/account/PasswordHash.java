package account;

import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 *
 * @author Jiyansh Agarwal
 */
public class PasswordHash {

    private static final int ITERATIONS = 10_000;
    private static final int SALT_LENGTH = 32;
    private static final int HASH_LENGTH = 256;

    /**
     * Makes a random 32 byte salt.
     *
     * @return The salt in a byte array.
     */
    private static byte[] makeSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a non-empty char array using PBKDF2 key derivation function with a random salt. Both the hash and salt will be 32 bytes each. The array
     * is written over after use for security.
     *
     * @param password The text to be hashed.
     * @return The salt concatenated with the hashed string. This is encoded to a string using the ISO-8859-1 character set.
     * @throws IllegalArgumentException No blank passwords.
     */
    public static String hashPasswordWithSalt(final char[] password) throws IllegalArgumentException {
        byte[] salt = makeSalt();

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Blank passwords are not allowed!");
        }

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_LENGTH);
            SecretKey key = factory.generateSecret(spec);

            String hashWithSalt = Base64.getEncoder().encodeToString(salt) + " : " + Base64.getEncoder().encodeToString(key.getEncoded());
            return hashWithSalt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } finally {
            wipePassword(password);
        }
    }

    /**
     * Checks if the hash of a char array matches an existing hash.
     *
     * @param password The text to match.
     * @param hashToMatch The hash to match.
     * @return True only if the hash of the char array is equal to hashToMatch.
     */
    public static boolean matches(final char[] password, String hashToMatch) {
        String salt = hashToMatch.split(" : ")[0];
        String hash = hashPasswordWithSalt(password, salt);

        if (hash.split(" : ")[1].equals(hashToMatch.split(" : ")[1])) {
            wipePassword(password);
            return true;
        }
        return false;
    }

    /**
     * Makes the input string a certain character length long using a filler <code>char</code> where necessary to fill spaces.
     *
     * @param str The string to normalize length.
     * @param length The length to make the string.
     * @param filler The char used to fill string if needed.
     * @return The normalized string.
     */
    public static String normalizeStringLength(String str, int length, char filler) {
        if (str == null) {
            throw new NullPointerException("Null string ");
        }
        if (str.length() == length) {
            return str;
        } else if (str.length() > length) {
            return str.substring(0, length);
        } else {
            return str + new String(new char[length - str.length()]).replace("\0", Character.toString(filler)); //Fills string with filler.
        }
    }

    /**
     * Improves security by overwriting a password array with zeros.
     * @param password the password to wipe.
     */
    public static void wipePassword(char[] password) {
        for (int i = 0; i < password.length; i++) {
            password[i] = 0;
        }
    }

    /**
     * Hashes a non-empty char array using PBKDF2 key derivation function. The hash will be 32 bytes. The array is written over after use for
     * security.
     *
     * @param password The text to be hashed.
     * @param salt The 32 byte string to use in the hash function.
     * @return The salt concatenated with the hashed string. The hash is encoded to a string using the ISO-8859-1 character set.
     * @throws IllegalArgumentException
     */
    private static String hashPasswordWithSalt(final char[] password, String salt) throws IllegalArgumentException {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Blank passwords are not allowed!");
        }

        byte[] byteSalt = Base64.getDecoder().decode(salt);

        if (byteSalt.length != 32) {
            throw new IllegalArgumentException("Salt must be 32 bytes long!");
        }

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, byteSalt, ITERATIONS, HASH_LENGTH);
            SecretKey key = factory.generateSecret(spec);

            String hashWithSalt = salt + " : " + Base64.getEncoder().encodeToString(key.getEncoded());
            return hashWithSalt;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } finally {
            wipePassword(password);
        }
    }
}
