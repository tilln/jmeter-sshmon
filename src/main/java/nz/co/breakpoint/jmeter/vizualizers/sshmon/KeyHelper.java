package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class KeyHelper {

    public static KeyPair pemToKeyPair(byte[] keyBytes, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
        EncryptedPrivateKeyInfo pkInfo = new EncryptedPrivateKeyInfo(keyBytes);
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());

        SecretKeyFactory pbeKeyFactory = SecretKeyFactory.getInstance(pkInfo.getAlgName());
        PKCS8EncodedKeySpec encodedKeySpec = pkInfo.getKeySpec(pbeKeyFactory.generateSecret(keySpec));

        PrivateKey privateKey = rsaKeyFactory.generatePrivate(encodedKeySpec);
        PublicKey publicKey = rsaKeyFactory.generatePublic(new RSAPublicKeySpec(
                ((RSAPrivateCrtKey) privateKey).getModulus(),
                ((RSAPrivateCrtKey) privateKey).getPublicExponent()));

        return new KeyPair(publicKey, privateKey);
    }
}
