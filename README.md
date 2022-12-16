This sample reproduces https://github.com/spring-projects/spring-security/issues/12378.

#### Steps to reproduce

```shell
./gradlew test 
```

#### Step 1
Running the prior command will execute the reproduction test. Expect the test to fail with 
`org.springframework.messaging.simp.stomp.ConnectionLostException: Connection closed`. There will be a debug log with
`java.lang.IllegalStateException: The request object has been recycled and is no longer associated with this facade`.
See below for full stack trace.

#### Step 2 - Force loading of CsrfToken while the HttpServletRequest is still active 
To get past the issue with the CsrfToken loading being deferred uncomment the interceptor in `org.example.WebSocketConfiguration`. 
Expect the test to fail with`org.springframework.messaging.simp.stomp.ConnectionLostException: Connection closed`. There will be 
a different debug exception with `org.springframework.security.web.csrf.InvalidCsrfTokenException: Invalid CSRF Token`. 

#### Step 3 - Revert to prior default CsrfTokenRequestHandler 
To get past the issue with the CsrfToken loading being deferred uncomment the interceptor in `org.example.SecurityConfiguration`.
This will finally allow the Stomp client to connect with CSRF validation minus BREECH protection.

### Debug Log Messages

Log level for `StompSubProtocolHandler` has been set to debug to display the exception in the logs.
```text
2022-12-16T13:27:24.860-06:00 DEBUG 10413 --- [o-auto-1-exec-5] o.s.w.s.m.StompSubProtocolHandler        : Failed to send message to MessageChannel in session 9f8620b1-fd66-b48d-9bd7-e15db9383356

org.springframework.messaging.MessageDeliveryException: Failed to send message to ExecutorSubscribableChannel[clientInboundChannel]
	at org.springframework.messaging.support.AbstractMessageChannel.send(AbstractMessageChannel.java:149) ~[spring-messaging-6.0.2.jar:6.0.2]
	at org.springframework.messaging.support.AbstractMessageChannel.send(AbstractMessageChannel.java:125) ~[spring-messaging-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.messaging.StompSubProtocolHandler.handleMessageFromClient(StompSubProtocolHandler.java:310) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.messaging.SubProtocolWebSocketHandler.handleMessage(SubProtocolWebSocketHandler.java:335) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.handler.WebSocketHandlerDecorator.handleMessage(WebSocketHandlerDecorator.java:75) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator.handleMessage(LoggingWebSocketHandlerDecorator.java:56) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.handler.ExceptionWebSocketHandlerDecorator.handleMessage(ExceptionWebSocketHandlerDecorator.java:58) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.adapter.standard.StandardWebSocketHandlerAdapter.handleTextMessage(StandardWebSocketHandlerAdapter.java:113) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.adapter.standard.StandardWebSocketHandlerAdapter$3.onMessage(StandardWebSocketHandlerAdapter.java:84) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.adapter.standard.StandardWebSocketHandlerAdapter$3.onMessage(StandardWebSocketHandlerAdapter.java:81) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.apache.tomcat.websocket.WsFrameBase.sendMessageText(WsFrameBase.java:415) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.sendMessageText(WsFrameServer.java:129) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.WsFrameBase.processDataText(WsFrameBase.java:515) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.WsFrameBase.processData(WsFrameBase.java:301) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.WsFrameBase.processInputBuffer(WsFrameBase.java:133) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.onDataAvailable(WsFrameServer.java:85) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.doOnDataAvailable(WsFrameServer.java:183) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.notifyDataAvailable(WsFrameServer.java:162) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsHttpUpgradeHandler.upgradeDispatch(WsHttpUpgradeHandler.java:157) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.coyote.http11.upgrade.UpgradeProcessorInternal.dispatch(UpgradeProcessorInternal.java:60) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:59) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:861) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1739) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at java.base/java.lang.Thread.run(Thread.java:833) ~[na:na]
Caused by: java.lang.IllegalStateException: The request object has been recycled and is no longer associated with this facade
	at org.apache.catalina.connector.RequestFacade.getSession(RequestFacade.java:889) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at jakarta.servlet.http.HttpServletRequestWrapper.getSession(HttpServletRequestWrapper.java:244) ~[tomcat-embed-core-10.1.1.jar:6.0]
	at jakarta.servlet.http.HttpServletRequestWrapper.getSession(HttpServletRequestWrapper.java:244) ~[tomcat-embed-core-10.1.1.jar:6.0]
	at org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.loadToken(HttpSessionCsrfTokenRepository.java:65) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.LazyCsrfTokenRepository.loadToken(LazyCsrfTokenRepository.java:98) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.RepositoryDeferredCsrfToken.init(RepositoryDeferredCsrfToken.java:63) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.RepositoryDeferredCsrfToken.get(RepositoryDeferredCsrfToken.java:48) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler.lambda$deferCsrfTokenUpdate$0(XorCsrfTokenRequestAttributeHandler.java:63) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler$CachedCsrfTokenSupplier.get(XorCsrfTokenRequestAttributeHandler.java:139) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler$CachedCsrfTokenSupplier.get(XorCsrfTokenRequestAttributeHandler.java:126) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler$SupplierCsrfToken.getDelegate(CsrfTokenRequestAttributeHandler.java:89) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler$SupplierCsrfToken.getHeaderName(CsrfTokenRequestAttributeHandler.java:75) ~[spring-security-web-6.0.0.jar:6.0.0]
	at org.springframework.security.messaging.web.csrf.CsrfChannelInterceptor.preSend(CsrfChannelInterceptor.java:56) ~[spring-security-messaging-6.0.0.jar:6.0.0]
	at org.springframework.messaging.support.AbstractMessageChannel$ChannelInterceptorChain.applyPreSend(AbstractMessageChannel.java:181) ~[spring-messaging-6.0.2.jar:6.0.2]
	at org.springframework.messaging.support.AbstractMessageChannel.send(AbstractMessageChannel.java:135) ~[spring-messaging-6.0.2.jar:6.0.2]
	... 27 common frames omitted
```


```text
2022-12-16T13:37:20.076-06:00 DEBUG 10933 --- [o-auto-1-exec-5] o.s.w.s.m.StompSubProtocolHandler        : Failed to send message to MessageChannel in session 222b96f9-570c-be02-37e5-6ddcf6fccc5b

org.springframework.messaging.MessageDeliveryException: Failed to send message to ExecutorSubscribableChannel[clientInboundChannel]
	at org.springframework.messaging.support.AbstractMessageChannel.send(AbstractMessageChannel.java:149) ~[spring-messaging-6.0.2.jar:6.0.2]
	at org.springframework.messaging.support.AbstractMessageChannel.send(AbstractMessageChannel.java:125) ~[spring-messaging-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.messaging.StompSubProtocolHandler.handleMessageFromClient(StompSubProtocolHandler.java:310) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.messaging.SubProtocolWebSocketHandler.handleMessage(SubProtocolWebSocketHandler.java:335) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.handler.WebSocketHandlerDecorator.handleMessage(WebSocketHandlerDecorator.java:75) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator.handleMessage(LoggingWebSocketHandlerDecorator.java:56) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.handler.ExceptionWebSocketHandlerDecorator.handleMessage(ExceptionWebSocketHandlerDecorator.java:58) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.adapter.standard.StandardWebSocketHandlerAdapter.handleTextMessage(StandardWebSocketHandlerAdapter.java:113) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.adapter.standard.StandardWebSocketHandlerAdapter$3.onMessage(StandardWebSocketHandlerAdapter.java:84) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.springframework.web.socket.adapter.standard.StandardWebSocketHandlerAdapter$3.onMessage(StandardWebSocketHandlerAdapter.java:81) ~[spring-websocket-6.0.2.jar:6.0.2]
	at org.apache.tomcat.websocket.WsFrameBase.sendMessageText(WsFrameBase.java:415) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.sendMessageText(WsFrameServer.java:129) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.WsFrameBase.processDataText(WsFrameBase.java:515) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.WsFrameBase.processData(WsFrameBase.java:301) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.WsFrameBase.processInputBuffer(WsFrameBase.java:133) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.onDataAvailable(WsFrameServer.java:85) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.doOnDataAvailable(WsFrameServer.java:183) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsFrameServer.notifyDataAvailable(WsFrameServer.java:162) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.tomcat.websocket.server.WsHttpUpgradeHandler.upgradeDispatch(WsHttpUpgradeHandler.java:157) ~[tomcat-embed-websocket-10.1.1.jar:10.1.1]
	at org.apache.coyote.http11.upgrade.UpgradeProcessorInternal.dispatch(UpgradeProcessorInternal.java:60) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:59) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:861) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1739) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) ~[tomcat-embed-core-10.1.1.jar:10.1.1]
	at java.base/java.lang.Thread.run(Thread.java:833) ~[na:na]
Caused by: org.springframework.security.web.csrf.InvalidCsrfTokenException: Invalid CSRF Token 'iOOv-2NYJfLeLZyURT67RDcOPcC9etPAI2pWG1LTwz997BLAv4ebnVA7FJfzHK7xchOPdFM2EPmOHubtFlszLDDg9FxJ1SDz' was found on the request parameter '_csrf' or header 'X-CSRF-TOKEN'.
	at org.springframework.security.messaging.web.csrf.CsrfChannelInterceptor.preSend(CsrfChannelInterceptor.java:59) ~[spring-security-messaging-6.0.0.jar:6.0.0]
	at org.springframework.messaging.support.AbstractMessageChannel$ChannelInterceptorChain.applyPreSend(AbstractMessageChannel.java:181) ~[spring-messaging-6.0.2.jar:6.0.2]
	at org.springframework.messaging.support.AbstractMessageChannel.send(AbstractMessageChannel.java:135) ~[spring-messaging-6.0.2.jar:6.0.2]
	... 27 common frames omitted
```
