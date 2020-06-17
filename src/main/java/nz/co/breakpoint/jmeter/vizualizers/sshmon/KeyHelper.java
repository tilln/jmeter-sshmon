package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.loader.pem.PEMResourceParserUtils;
import org.apache.sshd.common.config.keys.loader.pem.RSAPEMResourceKeyPairParser;
import org.apache.sshd.common.util.security.SecurityUtils;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

// TODO check out PEMResourceParserUtils
public class KeyHelper {

    public static KeyPair pemToKeyPair(byte[] keyBytes, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
//        return RSAPEMResourceKeyPairParser.decodeRSAKeyPair(SecurityUtils.getKeyFactory(KeyUtils.RSA_ALGORITHM),
//                new ByteArrayInputStream(keyBytes), true);
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
