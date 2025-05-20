package org.cardanofoundation.metabus.configuration.webclient;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(value = {WebClientProperties.class})
public class WebClientConfig implements BeanFactoryAware {
    private BeanFactory beanFactory;
    private final WebClientProperties webClientProperties;

    public WebClientConfig(WebClientProperties webClientProperties) {
        this.webClientProperties = webClientProperties;
    }

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void onPostConstruct() {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        List<WebClientProperties.WebClient> webClientBeans = webClientProperties.getWebclients();
        for (WebClientProperties.WebClient webClientBean : webClientBeans) {
            // setup beans programmatically
            String beanName = webClientBean.getBeanName();
            Map<String, String> headers = webClientBean.getHeaders();

            WebClient webClient = WebClient.builder()
                    .baseUrl(webClientBean.getBaseUrl())
                    .defaultHeaders(httpHeaders -> headers.forEach(httpHeaders::add)).build();

            configurableBeanFactory.registerSingleton(beanName, webClient);
        }
    }
}
