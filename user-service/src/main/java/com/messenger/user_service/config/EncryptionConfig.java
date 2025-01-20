package com.messenger.user_service.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;


@Configuration
public class EncryptionConfig {

    @Bean
    public TextEncryptor textEncryptor() {
        Dotenv dotenv = Dotenv.configure().load();
        String password = dotenv.get("ENCRYPTION_PASSWORD");
        String salt = dotenv.get("ENCRYPTION_SALT");
        return Encryptors.text(password, salt);
    }
}
