/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.iap;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * For IAP keys
 *
 * @author: lWX916345
 * @since: 02-11-2020
 */
public class CipherUtil {
    private static final String TAG = CipherUtil.class.getName();
    private static final String SIGN_ALGORITHMS = "SHA256WithRSA";
    private static final String PUBLIC_KEY ="YOUR_IAP_PUBLIC_KEY";

    /**
     * the method to check the signature for the data returned from the interface
     *
     * @param content   Unsigned data
     * @param sign      the signature for content
     * @param publicKey the public of the application
     * @return boolean
     */
    public static boolean doCheck(String content, String sign, String publicKey) {
        if (TextUtils.isEmpty(publicKey)) {
            Log.e(TAG, "publicKey is null");
            return false;
        }

        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(sign)) {
            Log.e(TAG, "data is error");
            return false;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(publicKey, Base64.DEFAULT);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
            signature.initVerify(pubKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            boolean bverify = signature.verify(Base64.decode(sign, Base64.DEFAULT));
            return bverify;

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "docheck NoSuchAlgorithmException");
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, "docheck InvalidKeySpecException");
        } catch (InvalidKeyException e) {
            Log.e(TAG, "docheck InvalidKeyException");
        } catch (SignatureException e) {
            Log.e(TAG, "docheck SignatureException");
        }
        return false;
    }

    /**
     * get the publicKey of the application
     * During the encoding process, avoid storing the public key in clear text.
     *
     * @return publickey
     */
    public static String getPublicKey() {
        return PUBLIC_KEY;
    }
}
