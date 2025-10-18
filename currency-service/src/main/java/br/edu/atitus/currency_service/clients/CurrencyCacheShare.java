package br.edu.atitus.currency_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.edu.atitus.currency_service.entities.CurrencyEntity;

@FeignClient(name = "CurrencyCacheShare", 
url = "http://localhost:8765"
		)
public interface CurrencyCacheShare {
	@PostMapping("/currency/{nameCache}/{keyCache}")
	 CurrencyBCResponse storeCache(	@PathVariable String nameCache,
			 						@PathVariable String keyCache, 
			 						@RequestBody CurrencyEntity currency );

}
