package _Project.Mita.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration //外部通信するための道具「電話機」
public class ExternalRestClientConfig {
	
	@Value("${app.api.base-url}")
	private String baseUrl;//TODO 仮名を使用中

    @Bean
    public RestClient externalBookRestClient() {
    	System.out.println("★DEBUG baseUrl = " + baseUrl);
        return RestClient.builder()
        		.baseUrl(baseUrl)
        		.build();
    }
}