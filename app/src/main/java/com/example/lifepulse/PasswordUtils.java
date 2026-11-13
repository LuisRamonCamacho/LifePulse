package com.example.lifepulse;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int ITERACIONES = 150000;
    private static final int LONGITUD_HASH = 256;
    private static final int LONGITUD_SALT = 16;

    public static String generarHash(String password) throws Exception {

        byte[] salt = new byte[LONGITUD_SALT];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERACIONES,
                LONGITUD_HASH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hash = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();

        return "pbkdf2_sha1$" + ITERACIONES + "$"
                + Base64.encodeToString(salt, Base64.NO_WRAP) + "$"
                + Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    public static boolean verificarPassword(
            String password,
            String hashGuardado) throws Exception {

        String[] partes = hashGuardado.split("\\$");

        if (partes.length != 4 ||
                !partes[0].equals("pbkdf2_sha1")) {
            return false;
        }

        int iteraciones = Integer.parseInt(partes[1]);

        byte[] salt = Base64.decode(partes[2], Base64.NO_WRAP);
        byte[] hashOriginal = Base64.decode(partes[3], Base64.NO_WRAP);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iteraciones,
                hashOriginal.length * 8
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hashNuevo = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();

        return MessageDigest.isEqual(hashOriginal, hashNuevo);
    }
}
