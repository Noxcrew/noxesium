package com.noxcrew.noxesium.api.util;

import com.noxcrew.noxesium.api.NoxesiumApi;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Assists in symmetric encryption and decryption of strings with AES keys.
 */
public class EncryptionUtil {
    /**
     * The initialization vector to use when encrypting.
     */
    private static final byte[] IV_PARAMETERS =
            new byte[] {-76, 14, 22, -123, 63, 60, -50, 23, -118, 10, 105, -127, 85, 41, -97, 37};

    /**
     * Encrypts the given input with the given file.
     * If the file is missing or the encryption fails, the raw input is used.
     */
    public static List<String> encrypt(@Nullable URL encryptionKeyFile, List<String> input) {
        if (encryptionKeyFile != null) {
            try {
                try (var stream = encryptionKeyFile.openStream()) {
                    byte[] keyBytes = Base64.getDecoder().decode(stream.readAllBytes());
                    var keySpec = new SecretKeySpec(keyBytes, "AES");
                    var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(IV_PARAMETERS));
                    var values = new ArrayList<String>();
                    for (var value : input) {
                        values.add(Base64.getEncoder()
                                .encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8))));
                    }
                    return values;
                }
            } catch (Exception x) {
                NoxesiumApi.getLogger().error("Failed to encrypt '{}'", input);
            }
        }
        return input;
    }

    /**
     * Encrypts the given input with the given file, if possible.
     * If the file is missing or the decryption fails, it is assumed to not be encrypted.
     */
    public static String decrypt(@Nullable URL encryptionKeyFile, String input) {
        if (encryptionKeyFile != null) {
            try {
                try (var stream = encryptionKeyFile.openStream()) {
                    byte[] keyBytes = Base64.getDecoder().decode(stream.readAllBytes());
                    var keySpec = new SecretKeySpec(keyBytes, "AES");
                    var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(IV_PARAMETERS));
                    return new String(cipher.doFinal(Base64.getDecoder().decode(input)), StandardCharsets.UTF_8);
                }
            } catch (Exception x) {
                NoxesiumApi.getLogger().error("Failed to decrypt '{}'", input);
            }
        }
        return input;
    }
}
