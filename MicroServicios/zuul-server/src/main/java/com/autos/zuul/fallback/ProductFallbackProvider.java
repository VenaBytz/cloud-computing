package com.autos.zuul.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FallbackProvider para product-service a nivel de ruteo de Zuul.
 * Se activa automáticamente cuando Zuul no puede alcanzar product-service
 * (servicio caído, timeout, error de red).
 */
@Component
public class ProductFallbackProvider implements FallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(ProductFallbackProvider.class);

    @Override
    public String getRoute() {
        return "product-service"; // Aplica solo a rutas de product-service
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        logger.warn("[FALLBACK-ZUUL] product-service no disponible. Causa: {}",
                cause != null ? cause.getMessage() : "desconocida");

        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return HttpStatus.SERVICE_UNAVAILABLE.value();
            }

            @Override
            public String getStatusText() throws IOException {
                return "Service Unavailable";
            }

            @Override
            public void close() {}

            @Override
            public InputStream getBody() throws IOException {
                String body = "{"
                        + "\"fallback\":true,"
                        + "\"servicio\":\"product-service\","
                        + "\"mensaje\":\"El servicio de productos no está disponible. Intente más tarde.\","
                        + "\"origen\":\"Zuul FallbackProvider\""
                        + "}";
                return new ByteArrayInputStream(body.getBytes("UTF-8"));
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }
}
