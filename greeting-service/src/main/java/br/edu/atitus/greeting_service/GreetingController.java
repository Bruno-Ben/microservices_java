package br.edu.atitus.greeting_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import br.edu.atitus.greeting_service.configs.GreetingConfig;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("greeting")
public class GreetingController {
	

	private final GreetingConfig config;//Essa classe depende disso
	
	public GreetingController(GreetingConfig config) {
		super();
		this.config = config;
	}

	@GetMapping("{pathName}")
	public ResponseEntity<String> greet(
			@RequestParam(required = false) String name,
			@PathVariable(required = false) String pathName )
	{


		String greetingReturn = config.getGreeting();
		String nameReturn = name != null && !name.isBlank()  ? name : config.getDefaultName();
		String textReturn = String.format("%s, %s!!!", greetingReturn, nameReturn);
		
		return ResponseEntity.ok(textReturn + pathName);
	}

}
