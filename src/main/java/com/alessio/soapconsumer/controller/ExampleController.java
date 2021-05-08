package com.alessio.soapconsumer.controller;

import com.alessio.service.Greeting;
import com.alessio.soapconsumer.externalservice.ExampleWSImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

    private final ExampleWSImpl exampleWS;

    public ExampleController(ExampleWSImpl exampleWS) {
        this.exampleWS = exampleWS;
    }

    @GetMapping("/exampleWs")
    public String callWs() {
        Greeting ciao = exampleWS.sayHello("Ciao");
        return ciao.getMessage();
    }
}
