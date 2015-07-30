package net.nueca.imonggosdk.tools;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ProductListTools {

    private static int line_no = 1;

    public static int getLineNo() {
        return line_no++;
    }

    public static void restartLineNo() {
        line_no = 1;
    }

    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return "";
        }
    }

}
