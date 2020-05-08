package com.blunt.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

import com.blunt.gateway.error.BluntException;
import com.blunt.gateway.filter.AuthenticationFilter.Config;
import com.blunt.gateway.util.BluntConstant;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends
    AbstractGatewayFilterFactory<Config> {

  Map<String, String> tokenCache = new HashMap();

  public AuthenticationFilter() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      if (!doAuthorize(request)) {
        throw new BluntException(BluntConstant.INVALID_CREDENTIAL, HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED);
      }
      ServerHttpResponse response = exchange.getResponse();
      if (exchange.getResponse().getStatusCode().equals(HttpStatus.OK) &&
          exchange.getRequest().getPath().toString().equals("/api/v1/authenticate/blunt")) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        System.out.println(headers.get("BLUNT-ID"));
        return chain.filter(exchange);
      }
      return chain.filter(exchange);
    };
  }

  private boolean doAuthorize(ServerHttpRequest request) {
    if (StringUtils.contains(request.getPath().toString(), "secured")) {
      return validateJwt(request);
    }
    return true;
  }

  private boolean validateJwt(ServerHttpRequest request) {
    final List<String> authorization = request.getHeaders().get("Authorization");
    String bearerToken = CollectionUtils.isEmpty(authorization) ? null : authorization.get(0);
    String token = StringUtils.isEmpty(bearerToken) ? null : bearerToken.split("Bearer ")[1];
    String bluntId = StringUtils.isEmpty(token) ? token : tokenCache.get(token);
    if (StringUtils.isEmpty(bluntId)) {
      return false;
    }
    request.mutate().header("BLUNT-ID", bluntId).build();
    return true;
  }

  public static class Config {

  }
}

