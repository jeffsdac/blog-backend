package br.com.jeffsdac.blog.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping({ "/swagger-ui", "/swagger-ui/" })
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui.html";
    }
}
