package br.com.jeffsdac.blog.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/hearthbeat")
public class Heartbeat {

    @GetMapping
    public ResponseEntity<String> alive() {
        return ResponseEntity.status(200).body("I'm alive");
    }

}
