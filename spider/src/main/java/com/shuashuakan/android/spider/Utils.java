package com.shuashuakan.android.spider;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String HASH_ALGORITHM_MD5 = "MD5";
    private static final String HASH_ALGORITHM_SHA1 = "SHA-1";

    private Utils() {
    }

    public static boolean isBlank(CharSequence string) {
        return string == null || string.toString().trim().length() == 0;
    }

    public static String valueOrDefault(String string, String defaultString) {
        return isBlank(string) ? defaultString : string;
    }

    public static String nullToEmpty(@Nullable String string) {
        return string == null ? "" : string;
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        } else {
            return reference;
        }
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return list;
    }

    public static byte[] sha256HMAC(String message, String secret) throws IOException {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            return sha256HMAC.doFinal(message.getBytes(UTF_8));
        } catch (InvalidKeyException | NoSuchAlgorithmException var4) {
            throw new IOException(var4);
        }
    }

    public static String md5hash(String key) {
        return hashWithAlgorithm("MD5", key);
    }

    public static String sha1hash(String key) {
        return hashWithAlgorithm("SHA-1", key);
    }

    public static String sha1hash(byte[] bytes) {
        return hashWithAlgorithm("SHA-1", bytes);
    }

    private static String hashWithAlgorithm(String algorithm, String key) {
        return hashWithAlgorithm(algorithm, key.getBytes(UTF_8));
    }

    private static String hashWithAlgorithm(String algorithm, byte[] bytes) {
        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var4) {
            return null;
        }

        return hashBytes(hash, bytes);
    }

    private static String hashBytes(MessageDigest hash, byte[] bytes) {
        hash.update(bytes);
        byte[] digest = hash.digest();
        StringBuilder builder = new StringBuilder();
        byte[] var4 = digest;
        int var5 = digest.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            int b = var4[var6];
            builder.append(Integer.toHexString(b >> 4 & 15));
            builder.append(Integer.toHexString(b >> 0 & 15));
        }

        return builder.toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
            }
        }
    }
}
