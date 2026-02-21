package org.example.util;

public final class Validators {


    private Validators() {}

    /**
     * arunca exceptie daca o conditie nu este indeplinita.
     * @param cond conditia care trebuie sa fie true.
     * @param message mesajul exceptiei daca conditia este false.
     * @return nu returneaza nimic.
     * @throws IllegalArgumentException daca cond este false.
     */
    public static void require(boolean cond, String message) {
        if (!cond) throw new IllegalArgumentException(message);
    }

    /**
     * verifica daca un string seamana cu un email (validare simpla nu completa).
     *
     * @param email textul de verificat.
     * @return true daca pare email altfel false.
     */
    public static boolean isEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        return e.contains("@") && e.contains(".") && e.length() >= 6;
    }

    /**
     * intoarce string gol daca valoarea este null altfel intoarce stringul original.
     *
     * @param s string posibil null.
     * @return string non null ("" daca s este null).
     */
    public static String nvl(String s) {
        return s == null ? "" : s;
    }
}