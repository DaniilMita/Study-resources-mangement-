package org.example.service;


import org.example.dao.UserDao;
import org.example.model.User;
import org.example.util.Crypto;
import org.example.util.Validators;

public final class AuthService {


    private final UserDao userDao = new UserDao();

    /**
     * autentifica un utilizator pe baza emailului si parolei.
     * @param email emailul introdus de utilizator.
     * @param password parola introdusa de utilizator
     * @return user daca autentificarea reuseste altfel null.
     * @throws IllegalArgumentException daca emailul/parola sunt invalide
     */
    public User login(String email, String password) {
        Validators.require(Validators.isEmail(email), "Email invalid.");
        Validators.require(password != null && password.length() >= 4, "Parola prea scurta.");
        User u = userDao.findByEmail(email.trim());
        if (u == null) return null;
        String hash = Crypto.sha256(password);
        if (!hash.equals(u.passwordHash())) return null;
        return u;
    }

    /**
     * inregistreaza un utilizator nou si il returneaza din db.
     * @param email emailul pentru cont.
     * @param password parola pentru cont.
     * @return user creat (citit din db dupa inserare).
     * @throws IllegalArgumentException daca emailul/parola sunt invalide sau emailul este deja folosit.
     */
    public User register(String email, String password) {
        Validators.require(Validators.isEmail(email), "Email invalid.");
        Validators.require(password != null && password.length() >= 4, "Parola prea scurta (minim 4).");
        String e = email.trim();
        if (userDao.findByEmail(e) != null) throw new IllegalArgumentException("Email deja folosit.");
        long id = userDao.insert(e, Crypto.sha256(password));
        return userDao.findById(id);
    }
}
