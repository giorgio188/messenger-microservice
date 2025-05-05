package com.messenger.chat_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Configuration
public class EncryptionConfig {
    @Value("${encryption.secret:defaultSecretKeyForEncryption}")
    private String encryptionKey;

    @Bean
    public Key secretKey() {
        byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] fixedKey = new byte[32];

        if (key.length < 32) {
            System.arraycopy(key, 0, fixedKey, 0, key.length);
        }
        else {
            System.arraycopy(key, 0, fixedKey, 0, 32);
        }

        return new SecretKeySpec(fixedKey, "AES");
    }
}
