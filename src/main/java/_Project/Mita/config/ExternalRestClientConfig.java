package _Project.Mita.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration //外部通信するための道具「電話機」
public class ExternalRestClientConfig {
	
	@Value("${app.api.base-url}")
	private String exBook;

    @Bean
    public RestClient externalBookRestClient() {
        
        return RestClient.builder()
        		.baseUrl(exBook)
        		.build();
    }
}