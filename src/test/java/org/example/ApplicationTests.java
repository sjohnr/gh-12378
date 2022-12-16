package org.example;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.CookieStore;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

    private static final ParameterizedTypeReference<Map<String, String>> CSRF_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    @Value("${local.server.port}")
    private String port;

    /**
     * Note: The http calls mimic calls that would normally occur in a browser.
     */
    @Test
    void run() throws Exception {
        String baseUrl = "127.0.0.1:" + this.port;
        String baseHttpUrl = "http://" + baseUrl;
        String wsUrl = "ws://" + baseUrl + "/stomp";
        CookieStore cookieStore = new HttpCookieStore();
        HttpClient httpClient = new HttpClient();
        httpClient.setCookieStore(cookieStore);
        WebClient webClient = WebClient.builder()
                .baseUrl(baseHttpUrl)
                // Using Jetty since allows for managing cookies across
                .clientConnector(new JettyClientHttpConnector(httpClient))
                .build();

        // Generate initial CSRF prior to authentication
        CsrfToken csrfToken = requestCsrfToken(webClient);

        // Authenticate User
        webClient
                .post().uri("/login")
                .body(BodyInserters.fromFormData("username", "user")
                        .with("password", "password"))
                .header(csrfToken.getHeaderName(), csrfToken.getToken())
                .retrieve().toBodilessEntity().block();


        // Get new CSRF token post authentication
        csrfToken = requestCsrfToken(webClient);


        JettyWebSocketClient webSocketClient = new JettyWebSocketClient(new WebSocketClient(httpClient));
        webSocketClient.start();

        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(webSocketClient);

        // Apply CSRF token as a STOMP CONNECTION header
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(csrfToken.getHeaderName(), csrfToken.getToken());

        CompletableFuture<StompSession> connectAsync = webSocketStompClient.connectAsync(new URI(wsUrl),
                null,
                connectHeaders,
                new StompSessionHandlerAdapter() {
                    public Type getPayloadType(StompHeaders headers) {
                        return super.getPayloadType(headers);
                    }
                });

        connectAsync.get();
    }

    private CsrfToken requestCsrfToken(WebClient webClient) {
        Map<String, String> csrfResponse = webClient
                .get().uri("/csrf")
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(CSRF_TYPE_REF))
                .block();
        Objects.requireNonNull(csrfResponse);

        return new DefaultCsrfToken(
                csrfResponse.get("headerName"),
                csrfResponse.get("parameterName"),
                csrfResponse.get("token")
        );
    }

}
