package com.ifsc.secstor.api;

import com.at.archistar.crypto.secretsharing.WeakSecurityException;
import com.ifsc.secstor.api.config.SecstorConfig;
import com.ifsc.secstor.api.service.SecretSharingImplementation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.security.NoSuchAlgorithmException;

@EnableConfigurationProperties(SecstorConfig.class)
@EnableCaching
@EntityScan(basePackages = "com.ifsc.secstor.api.model")
@SpringBootApplication(scanBasePackages = "com.ifsc.secstor.api")
@OpenAPIDefinition(
		info = @Info(
				title = "Secstor API",
				version = "1.0",
				description = "Api para aplicação de algoritmos de compartilhamento de segredos " +
						"e anonimização de dados para adequação de sistemas à LGPD. ")
)
public class SecstorApplication {
	public static void main(String[] args) {
		SpringApplication.run(SecstorApplication.class, args);
	}
}
