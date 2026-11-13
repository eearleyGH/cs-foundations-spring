package com.elishaearley.csfoundations.shortener.web;

import com.elishaearley.csfoundations.shortener.link.LinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/codes")
public class LinkController {
    private final LinkService links;

    public LinkController(LinkService links) {
        this.links = links;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LinkService.CreatedLink create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateLinkRequest body) {
        return links.create(Long.parseLong(jwt.getSubject()), body.longUrl());
    }

    @GetMapping("/{code}")
    public LinkService.LinkStats stats(@AuthenticationPrincipal Jwt jwt, @PathVariable String code) {
        return links.stats(code, Long.parseLong(jwt.getSubject()));
    }

    public record CreateLinkRequest(@NotBlank @URL String longUrl) {}
}
