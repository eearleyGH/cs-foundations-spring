package com.elishaearley.csfoundations.shortener.web;

import com.elishaearley.csfoundations.shortener.link.LinkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {
    private final LinkService links;

    public RedirectController(LinkService links) {
        this.links = links;
    }

    @GetMapping("/{code:[A-Za-z0-9]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = links.longUrlForRedirect(code);
        links.recordClick(code);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, longUrl).build();
    }
}
