package xiaozhi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;
import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

/**
 * WebClient配置类
 *
 * @author zjy
 * @since 2025-3-21
 */
@Configuration
public class WebClientConfig {

    @Value("${feign.voice-clone.url}")
    private String voiceCloneBaseUrl;

    /**
     * 配置语音克隆服务的WebClient
     */
    @Bean
    public WebClient voiceCloneWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(voiceCloneBaseUrl)
                .filter(errorHandlingFilter())
                .build();
    }

    /**
     * 错误处理过滤器
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(
                                new WebClientResponseException(
                                        clientResponse.statusCode().value(),
                                        clientResponse.statusCode().toString(),
                                        clientResponse.headers().asHttpHeaders(),
                                        errorBody.getBytes(),
                                        null,
                                        null
                                )
                        ));
            }
            return Mono.just(clientResponse);
        });
    }
} 