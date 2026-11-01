package com.example.cicd;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // CHANGE THIS TEXT for the redeployment demo (commit + push -> pipeline -> visible in browser)
    static final String MESSAGE = "Version 1 - first release";

    @GetMapping(value = "/", produces = "text/html")
    public String home() {
        return """
            <html>
              <head><title>CI/CD Demo</title></head>
              <body style="font-family: sans-serif; margin: 3rem;">
                <h1>CI/CD Demo</h1>
                <p>%s</p>
                <p><small>Served by pod: %s</small></p>
              </body>
            </html>
            """.formatted(MESSAGE, hostName());
    }

    @GetMapping("/api/message")
    public String message() {
        return MESSAGE;
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
