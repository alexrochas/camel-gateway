package br.com.alex.spring.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GatewayApplicationTests {

  @Test
  public void getWithCookie() throws IOException {
    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8081)); //No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();
    wireMockServer.stubFor(get(WireMock.anyUrl())
            .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withStatus(200)
                    .withBody("Worth it!")));

    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie = new BasicClientCookie("token", "1234");
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);

    HttpContext localContext = new BasicHttpContext();
    localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet getRequest = new HttpGet("http://localhost:8080/go");
    CloseableHttpResponse response = httpClient.execute(getRequest, localContext);
    String responseBody = EntityUtils.toString(response.getEntity());

    assertEquals(responseBody, "Worth it!");

    wireMockServer.stop();
  }

  @Test
  public void getWithoutCookie() throws IOException {
    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8081)); //No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();
    wireMockServer.stubFor(get(WireMock.anyUrl())
            .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withStatus(200)
                    .withBody("Worth it!")));

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpGet getRequest = new HttpGet("http://localhost:8080/go");
    CloseableHttpResponse response = httpClient.execute(getRequest);

    assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR_500);

    wireMockServer.stop();
  }
}
