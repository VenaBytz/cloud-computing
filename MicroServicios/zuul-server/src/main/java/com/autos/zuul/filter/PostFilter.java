package com.autos.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

/**
 * Filtro POST: se ejecuta DESPUÉS de que Zuul recibió la respuesta del microservicio.
 * Útil para modificar cabeceras de respuesta, logging de tiempos, métricas, etc.
 */
@Component
public class PostFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(PostFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE; // "post"
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        int status = ctx.getResponse().getStatus();

        logger.info("[POST-FILTER] Respuesta enviada al cliente | Código HTTP: {}", status);

        // Agregar cabecera de respuesta personalizada
        ctx.getResponse().addHeader("X-Zuul-Procesado", "true");

        return null;
    }
}
