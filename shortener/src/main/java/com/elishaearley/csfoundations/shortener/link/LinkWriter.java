package com.elishaearley.csfoundations.shortener.link;

import com.elishaearley.csfoundations.shortener.auth.UserAccount;
import com.elishaearley.csfoundations.shortener.auth.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LinkWriter {
    private final ShortLinkRepository links;
    private final UserAccountRepository users;

    public LinkWriter(ShortLinkRepository links, UserAccountRepository users) {
        this.links = links;
        this.users = users;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ShortLink insert(String code, String longUrl, long ownerId) {
        UserAccount owner = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
        return links.saveAndFlush(new ShortLink(code, longUrl, owner));
    }
}
