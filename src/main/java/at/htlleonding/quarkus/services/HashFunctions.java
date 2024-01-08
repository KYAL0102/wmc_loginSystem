package at.htlleonding.quarkus.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFunctions {
    public static String hashPassword(String password, String salt, String pepper) {
        try {
            String input = password + salt + pepper;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedhash = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Fehler beim Hashing", e);
        }
    }

    public static boolean checkPassword(String enteredPassword, String salt, String pepper, String storedHash) {
        String hashedPassword = hashPassword(enteredPassword, salt, pepper);

        return hashedPassword.equals(storedHash);
    }

}
