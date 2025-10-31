package br.edu.atitus.product_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.clients.CurrencyClient;
import br.edu.atitus.product_service.clients.CurrencyResponse;
import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;

@RestController
@RequestMapping("products")
public class OpenProductController {
	
	private final ProductRepository repository;
	private final CurrencyClient currencyClient;
	private final CacheManager cacheManager;
	
	public OpenProductController(ProductRepository repository, CurrencyClient currencyClient, CacheManager cacheManager) {
		super();
		this.repository = repository;
		this.currencyClient = currencyClient;
		this.cacheManager = cacheManager;
	}
	
	@Value("${server.port}")
	private int serverPort;
	
	@Value("${HOSTNAME:}")
	private String hostName;
	
	
	
	@GetMapping("/{idProduct}/{targetCurrency}")
	public ResponseEntity<ProductEntity> getProduct(
			@PathVariable Long idProduct,
			@PathVariable String targetCurrency
 			) throws Exception {
		
		String nameCacheProductId = "idProduct";
		String nameCacheTargetCurrency = "targetCurrency";
		String environment = "Product-service running on port/instance: " + (hostName.isBlank() ? serverPort : hostName) ;
		ProductEntity product = cacheManager.getCache(nameCacheProductId).get(idProduct, ProductEntity.class);
		CurrencyResponse currency = new CurrencyResponse();
		if (product == null) {
			product = repository.findById(idProduct).orElseThrow(() -> new Exception("Product not found"));
			product.setEnvironment(environment + " - Product Source: Local Database");
		} else {
			product.setEnvironment(environment + " - Product Source: Cache");
		}
		
		if (targetCurrency.equalsIgnoreCase(product.getCurrency()))
			product.setConvertedPrice(product.getPrice());
		else {
			try {
				currency = cacheManager.getCache(nameCacheTargetCurrency).get(targetCurrency, CurrencyResponse.class);
				
				if (currency == null) {
					currency = currencyClient.getCurrency(
								product.getPrice(), 
								product.getCurrency(), 
								targetCurrency);
					String currencyDataSource = currency.getEnvironment().split("DataSource:")[1];
					product.setEnvironment(product.getEnvironment() + " -  Currency Source: " +  currencyDataSource);

				} else {

					product.setEnvironment(product.getEnvironment() + " -  Currency Source: Cache");
				}
	
				product.setConvertedPrice(currency.getConvertedValue());
				
				} catch(Exception e) {
					product.setConvertedPrice(-1);
				}

			}
		
	
		cacheManager.getCache(nameCacheProductId).put(product.getId(), product);
		cacheManager.getCache(nameCacheTargetCurrency).put(targetCurrency, currency);
		return ResponseEntity.ok(product);
	}
	
	@GetMapping("/noconverter/{idProduct}")
	public ResponseEntity<ProductEntity> getNoConverter(@PathVariable Long idProduct) throws Exception{
		var product = repository.findById(idProduct).orElseThrow(() -> new Exception("Produto não encontrado"));
		String environment = "Product-service running on port/instance: " + (hostName.isBlank() ? serverPort : hostName) ;
		product.setConvertedPrice(-1);
		product.setEnvironment(environment);
		return ResponseEntity.ok(product);
	}
	
	@GetMapping("/{targetCurrency}")
	public ResponseEntity<Page<ProductEntity>> getAllProducts(
		    @PathVariable String targetCurrency,
		    @PageableDefault(page = 0, size = 5, sort = "description", direction = Direction.ASC)
		    Pageable pageable) throws Exception {
			String environment = "Product-service running on port/instance: " + (hostName.isBlank() ? serverPort : hostName) ;
		    Page<ProductEntity> products = repository.findAll(pageable);
		    for (ProductEntity product : products) {
		        CurrencyResponse currency = currencyClient.getCurrency(product.getPrice(), product.getCurrency(), targetCurrency);

		        product.setConvertedPrice(currency.getConvertedValue());
		        
		        product.setEnvironment(environment + " - " + product.getEnvironment()); // + " - " + cambio.getAmbiente());
		    }
		    return ResponseEntity.ok(products);
		}


}
