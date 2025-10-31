package br.edu.atitus.currency_service.controllers;

import java.sql.Timestamp;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.currency_service.clients.CurrencyBCClient;
import br.edu.atitus.currency_service.clients.CurrencyBCResponse;
import br.edu.atitus.currency_service.clients.CurrencyBCResponse.CurrencyBC;
import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;

@RestController
@RequestMapping("currency")
@EnableAsync
public class CurrencyController {
	
	private final CurrencyRepository repository;
	private final CurrencyBCClient currencyBCClient;
	private final CacheManager cacheManager;
	
	@Value("${server.port}")
	private int serverPort;
	
	@Value("${HOSTNAME:}")
	private String hostName;
	
	public CurrencyController(CurrencyRepository repository, CurrencyBCClient currencyBCClient, CacheManager cacheManager) {
		super();
		this.repository = repository;
		this.currencyBCClient = currencyBCClient;
		this.cacheManager = cacheManager;

	}
	

	@GetMapping("/{value}/{source}/{target}")
	public ResponseEntity<CurrencyEntity> getConversion(
			@PathVariable double value,
			@PathVariable String source,	
			@PathVariable String target) throws Exception{

		source = source.toUpperCase();
		target = target.toUpperCase();
		String dataSource = "None";
		String nameCache = "Currency";
		String keyCache = source + target;
		
		CurrencyEntity currency = cacheManager.getCache(nameCache).get(keyCache, CurrencyEntity.class);
		
		if (currency != null) {
			dataSource = "Cache";

		} else {
			currency = new CurrencyEntity();
			currency.setSource(source);
			currency.setTarget(target);
			if(source.equals(target)) {
				currency.setConversionRate(1);
			} else {
				try {
				double curSource = 1;
				double curTarget = 1;
				if (!source.equals("BRL")) {
					CurrencyBCResponse resp = currencyBCClient.getCurrency(source);
					if (resp.getValue().isEmpty()) throw new Exception("Currency not found for " + source);
					curSource = resp.getValue().stream()
					        .max(Comparator.comparing(c -> Timestamp.valueOf(c.getDataHoraCotacao())))
					        .map(CurrencyBC::getCotacaoVenda)
					        .orElse(null);
					
				}
				if (!target.equals("BRL")) {
					CurrencyBCResponse resp = currencyBCClient.getCurrency(target);
					if (resp.getValue().isEmpty()) throw new Exception("Currency not found for " + target);
					 curTarget = resp.getValue().stream()
						        .max(Comparator.comparing(c -> Timestamp.valueOf(c.getDataHoraCotacao())))
						        .map(CurrencyBC::getCotacaoVenda)
						        .orElse(null);
				}
				currency.setConversionRate(curSource / curTarget);
				dataSource = "API BCB";
				} catch(Exception e) {
					currency = repository.findBySourceAndTarget(source, target).orElseThrow(() -> new Exception("Currency Unsupported"));
					dataSource = "Local Database";
				}
				
				
			}
			cacheManager.getCache(nameCache).put(keyCache, currency);
			// Só salvar cache quando realmente buscar, essa chamada reseta o tempo de expiração desse cache
			
			final CurrencyEntity currencyFinal = currency;
			
				
		}
		
		
		currency.setConvertedValue(value * currency.getConversionRate());
		currency.setEnvironment("Currency running in port/instance: " + (hostName.isBlank() ? serverPort : hostName) + " - DataSource: " + dataSource);

		return ResponseEntity.ok(currency);
		
	}
	
	
	@PostMapping("/{nameCache}/{keyCache}")
	public void storeCache(
			@PathVariable String nameCache,
			@PathVariable String keyCache,
			@RequestBody CurrencyEntity currency) {
		cacheManager.getCache(nameCache).put(keyCache, currency);
	}
	
	
	

}
