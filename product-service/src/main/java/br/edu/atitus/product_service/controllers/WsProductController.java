package br.edu.atitus.product_service.controllers;

import javax.security.sasl.AuthenticationException;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.clients.CurrencyResponse;
import br.edu.atitus.product_service.dtos.ProductDTO;
import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;

@RestController
@RequestMapping("/ws/products")
public class WsProductController {
	
	private final ProductRepository repository;
	private final CacheManager cacheManager;

	public WsProductController(ProductRepository repository, CacheManager cacheManager) {
		super();
		this.repository = repository;
		this.cacheManager = cacheManager;
	}
	
	private ProductEntity convertDto2Entity(ProductDTO dto) {
		var product = new ProductEntity();
		BeanUtils.copyProperties(dto, product);
		return product;
	}
	
	@PostMapping
	public ResponseEntity<ProductEntity> post(
			@RequestBody ProductDTO dto,
			//@RequestHeader("X-User-id") Long userId,
		//	@RequestHeader("X-User-Email") String userEmail,
			@RequestHeader("X-User-Type") Integer userType) throws Exception {
		
		if(userType != 0)
			throw new AuthenticationException("Usuário sem permissão");
		
		var product = convertDto2Entity(dto);
		product.setStock(10);
		repository.save(product);
		return ResponseEntity.status(201).body(product);
	}
	
	@PutMapping("/{idProduct}")
	public ResponseEntity<ProductEntity> put(
			@PathVariable Long idProduct,
			@RequestBody ProductDTO dto,
			@RequestHeader("X-User-Type") Integer userType) throws Exception {
		
		if(userType != 0)
			throw new AuthenticationException("Usuário sem permissão");
		
		var product = convertDto2Entity(dto);
		product.setId(idProduct);
		product.setStock(10);
		repository.save(product);
		String nameCacheProductId = "idProduct";
		String nameCacheTargetCurrency = "targetCurrency";
		
		cacheManager.getCache(nameCacheTargetCurrency).evict(product.getCurrency());
		cacheManager.getCache(nameCacheProductId).evict(product.getId());
		
		return ResponseEntity.status(201).body(product);
	}
	
	@DeleteMapping("/{idProduct}")
	public ResponseEntity<String> delete(
			@PathVariable Long idProduct,
			@RequestHeader("X-User-Type") Integer userType) throws Exception {
		
		if(userType != 0)
			throw new AuthenticationException("Usuário sem permissão");
		String nameCacheProductId = "idProduct";
		String nameCacheTargetCurrency = "targetCurrency";
		cacheManager.getCache(nameCacheProductId).clear();
		
		repository.deleteById(idProduct);
		
		return ResponseEntity.ok("Excluído");
	}
		
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<String> handlerAuth(AuthenticationException e) {
		String message = e.getMessage().replaceAll("[\\r\\n]", "");
		return ResponseEntity.status(403).body(message);
	}

}
