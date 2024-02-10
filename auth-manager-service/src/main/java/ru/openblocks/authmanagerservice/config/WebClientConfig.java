package ru.openblocks.authmanagerservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 10000;

    @Bean
    public WebClient defaultWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIMEOUT_MILLISECONDS)
                .responseTimeout(Duration.ofMillis(DEFAULT_TIMEOUT_MILLISECONDS))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
