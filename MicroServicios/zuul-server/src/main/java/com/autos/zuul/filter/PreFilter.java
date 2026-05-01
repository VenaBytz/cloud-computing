package com.autos.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Filtro PRE: se ejecuta ANTES de que Zuul enrute la petición.
 * Útil para autenticación, logging, validación de cabeceras, etc.
 */
@Component
public class PreFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(PreFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE; // "pre"
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
        HttpServletRequest request = ctx.getRequest();

        logger.info("[PRE-FILTER] Método: {} | URI: {} | Servicio destino: {}",
                request.getMethod(),
                request.getRequestURI(),
                ctx.get(FilterConstants.SERVICE_ID_KEY));

        // Se puede agregar lógica de autenticación / validación de token aquí
        ctx.addZuulRequestHeader("X-Zuul-Gateway", "zuul-server");

        return null;
    }
}
