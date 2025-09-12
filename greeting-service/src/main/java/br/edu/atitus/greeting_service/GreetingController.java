package br.edu.atitus.greeting_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.greeting_service.configs.GreetingConfig;
import br.edu.atitus.greeting_service.dto.GreetingRequest;

@RestController
@RequestMapping("greeting")
public class GreetingController {
	
	private final GreetingConfig config;


	public GreetingController(GreetingConfig config) {
		this.config = config;
	}


	@GetMapping({"", "{pathName}"})
	public ResponseEntity<String> greet(
			@RequestParam(required = false) String name,
			@PathVariable(required = false) String pathName) {
		String greetingReturn = config.getGreeting();
		String nameReturn = (name != null && !name.isBlank()) ? name : config.getDefaultName();
		String nameToUse = (pathName != null && !pathName.isBlank()) ? pathName : nameReturn;
		String textReturn = String.format("%s, %s!!!", greetingReturn, nameToUse);
		
		return ResponseEntity.ok(textReturn);
	}
	
	@PostMapping("")
    public ResponseEntity<String> greet(@RequestBody GreetingRequest greetingRequest) {
        String greetingReturn = config.getGreeting();
        String nameReturn = (greetingRequest.getName() != null && !greetingRequest.getName().isBlank()) ? greetingRequest.getName() : config.getDefaultName();
        String textReturn = String.format("%s, %s!!!", greetingReturn, nameReturn);
        
        return ResponseEntity.ok(textReturn);
    }
}
