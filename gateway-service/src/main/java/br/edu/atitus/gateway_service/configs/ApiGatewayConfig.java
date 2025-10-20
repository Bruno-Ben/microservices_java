package br.edu.atitus.gateway_service.configs;


import br.edu.atitus.gateway_service.GatewayServiceApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

    private final GatewayServiceApplication gatewayServiceApplication;

    ApiGatewayConfig(GatewayServiceApplication gatewayServiceApplication) {
        this.gatewayServiceApplication = gatewayServiceApplication;
    }
	
	@Bean
	RouteLocator getGatewarRouter(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p
						.path("/get")
						.filters(f -> f
								.addRequestHeader("X-USER-NAME", "username")
								.addRequestParameter("name", "noname"))
						.uri("http://httpbin.org:80"))
				.route(p -> p
						.path("/products/**")
						.uri("lb://product-service"))
				.route(p -> p
						.path("/currency/**")
						.uri("lb://currency-service"))
				.route(p -> p
						.path("/auth/**")
						.uri("lb://auth-service"))
				.build();
	}

}
