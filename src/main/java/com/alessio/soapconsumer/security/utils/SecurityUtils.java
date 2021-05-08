//package com.alessio.soapconsumer.security.utils;
//
//import com.alessio.soapconsumer.security.aes.AESChiperType;
//import org.bouncycastle.crypto.digests.SHA256Digest;
//import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
//import org.bouncycastle.crypto.params.KeyParameter;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.UnsupportedEncodingException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.spec.InvalidKeySpecException;
//
//public class SecurityUtils {
//
//    private static final String CIPHER_TRIPLE = "AES/CBC/PKCS5Padding";
//    private static final String CIPHER_ALGORITHM = "AES";
//
//    private static final byte[] static_iv = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//
//    public static byte[] AES(byte[] valueToEncrypt, AESChiperType chiperType, byte[] key) throws Exception {
//        return AES(valueToEncrypt, chiperType, key, static_iv);
//    }
//
//    public static byte[] AES(byte[] valueToEncrypt, AESChiperType chiperType, byte[] key, byte[] custom_iv) throws Exception {
//
//        SecretKeySpec secretKeySpec1 = new SecretKeySpec(key, CIPHER_ALGORITHM);
//
//        Cipher c1 = Cipher.getInstance(CIPHER_TRIPLE);
//        int cryptoChiperType;
//        if (chiperType.equals(AESChiperType.DECRYPT)) {
//            cryptoChiperType = Cipher.DECRYPT_MODE;
//        } else if (chiperType.equals(AESChiperType.ENCRYPT)) {
//            cryptoChiperType = Cipher.ENCRYPT_MODE;
//        } else {
//            throw new Exception();
//        }
//
//        c1.init(cryptoChiperType, secretKeySpec1, new IvParameterSpec(custom_iv));
//        return c1.doFinal(valueToEncrypt);
//    }
//
//    public static byte[] PBKDF2WithHmacSHA256(byte[] password, byte[] salt, int iterationCount, int dkLenInBytes) throws InvalidKeySpecException, NoSuchAlgorithmException, UnsupportedEncodingException, Exception {
//
//        if (dkLenInBytes > 32) {
//            throw new Exception("Maximum length allowed for SHA256 is 32");
//        }
//
//        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
//        generator.init(password, salt, iterationCount);
//        KeyParameter key = (KeyParameter) generator.generateDerivedMacParameters(dkLenInBytes * 8);
//
//        return key.getKey();
//    }
//
//    public static byte[] getSalt() throws NoSuchAlgorithmException {
//        byte[] salt = new byte[16];
//        new SecureRandom().nextBytes(salt);
//        return salt;
//    }
//
//}
