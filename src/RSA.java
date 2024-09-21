import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA {

    private PrivateKey privateKey;
    public PublicKey publicKey;

    private String publicKeyString;
    private String privateKeyString;

    public void init() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void initFromStrings() {
        try {
            X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(decode(publicKeyString));


            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            if (publicKeyString!= null && !publicKeyString.isEmpty()) {
                publicKey = keyFactory.generatePublic(keySpecPublic);
            }
            if (privateKeyString != null && !privateKeyString.isEmpty()) {
                PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(decode(privateKeyString));
                privateKey = keyFactory.generatePrivate(keySpecPrivate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing RSA keys from strings", e);
        }
    }

    public void printKeys(){
        System.out.println("Public key: " + encode(publicKey.getEncoded()));
        System.out.println("Private key: " + encode(privateKey.getEncoded()));
    }

    public String encrypt(String message) throws Exception {
        byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        return encode(encryptedBytes);
    }

    private String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public String decrypt(String encryptedMessage) throws Exception{
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage,"UTF8");
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static void main(String[] args) {
        RSA rsa = new RSA();
        rsa.init();
        rsa.initFromStrings();
        try {
            String encryptedMessage = rsa.encrypt("Hello World");
            String decryptedMessage = rsa.decrypt(encryptedMessage);

            System.out.println("Encrypted: " + encryptedMessage);
            System.out.println("Decrypted: " + decryptedMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKeyString(String publicKeyString) {
        this.publicKeyString = publicKeyString;
    }
}