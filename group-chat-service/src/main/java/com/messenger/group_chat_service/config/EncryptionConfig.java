package com.messenger.group_chat_service.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;


@Configuration
public class EncryptionConfig {

    @Value("${encryption.password}")
    private String encryptorPassword;

    @Value("${encryption.salt}")
    private String encryptorSalt;

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.text(encryptorPassword, encryptorSalt);
    }
}
