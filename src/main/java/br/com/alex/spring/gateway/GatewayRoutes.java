package br.com.alex.spring.gateway;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GatewayRoutes extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("jetty:http://0.0.0.0:8080?matchOnUriPrefix=true")
            .validate((e) -> {
              Cookie[] cookies = e.getIn().getBody(HttpServletRequest.class).getCookies();
              return !Arrays.stream(Optional.ofNullable(cookies).orElse(new Cookie[]{}))
                      .filter(c -> c.getName().equals("token"))
                      .collect(Collectors.toList()).isEmpty();
            })
            .process((e) -> {
              e.getIn().setHeader("redirectUrl",
                              "localhost:8081" + e.getIn().getBody(HttpServletRequest.class).getContextPath());
            })
            .recipientList(simple("jetty:http://${header.redirectUrl}?bridgeEndpoint=true"))
            .marshal()
            .gzip();
  }
}
