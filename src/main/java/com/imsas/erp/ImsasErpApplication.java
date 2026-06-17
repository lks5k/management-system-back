package com.imsas.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada principal del sistema ERP de Imagen Marquillas SAS.
 *
 * <p>{@code @ConfigurationPropertiesScan} habilita el escaneo automático de clases
 * anotadas con {@code @ConfigurationProperties} en todo el paquete base,
 * evitando la necesidad de registrarlas manualmente con {@code @EnableConfigurationProperties}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ImsasErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImsasErpApplication.class, args);
    }
}
