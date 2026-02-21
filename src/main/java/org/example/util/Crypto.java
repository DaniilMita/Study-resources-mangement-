package org.example.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

    public final class Crypto {

        /** constructor privat: clasa utilitara nu se instantiaza. */
        private Crypto() {}

        /**
         * calculeaza hash-ul sha-256 pentru un text
         * si il returneaza ca string hex (lowercase).
         * @param s textul de intrare (ex. parola) care va fi hash-uit
         * @return hash-ul sha-256 in format hex (64 caractere).
         * @throws RuntimeException daca algoritmul sha-256
         * nu este disponibil sau apare o eroare la calcul.
         */
        public static String sha256(String s) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) sb.append(String.format("%02x", b));
                return sb.toString();
            } catch (Exception e) {
                throw new RuntimeException("sha256 esuat: " + e.getMessage(), e);
            }
        }
}
