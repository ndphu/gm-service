package vn.kms.phudnguyen.crawlers.vungtv.filter;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CorsFilter extends OncePerRequestFilter{

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    allow(request, response);

    if (!CorsUtils.isPreFlightRequest(request)) {
      filterChain.doFilter(request, response);
    }
  }
  public static void allow(HttpServletRequest request, HttpServletResponse response) {
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeader(HttpHeaders.ORIGIN));
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
        "Authorization, Accept, Accept-Encoding, Accept-Language, Connection, Content-Length, Content-Type, Cookie, DNT, Host, Origin, Referer, User-Agent,"
            + "X-Requested-With, Access-Control-Request-Method, "
            + "Access-Control-Request-Headers");
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE, HEAD, PATCH");
    response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "86400");
  }
}
