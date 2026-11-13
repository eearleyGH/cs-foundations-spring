package com.elishaearley.csfoundations.shortener.link;

import com.elishaearley.csfoundations.shortener.auth.UserAccountRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LinkService {
    private final ShortLinkRepository links;
    private final LinkWriter writer;
    private final UserAccountRepository users;
    private final CodeGenerator codes;
    private final String publicBaseUrl;

    public LinkService(
            ShortLinkRepository links,
            LinkWriter writer,
            UserAccountRepository users,
            CodeGenerator codes,
            String publicBaseUrl) {
        this.links = links;
        this.writer = writer;
        this.users = users;
        this.codes = codes;
        this.publicBaseUrl = publicBaseUrl;
    }

    public CreatedLink create(long ownerId, String longUrl) {
        if (users.findById(ownerId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user");
        }
        for (int attempt = 0; attempt < 8; attempt++) {
            try {
                ShortLink saved = writer.insert(codes.next(), longUrl, ownerId);
                return new CreatedLink(saved.getCode(), publicBaseUrl + "/" + saved.getCode());
            } catch (DataIntegrityViolationException duplicate) {
                // unique code collision; try another
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not allocate a unique code");
    }

    @Cacheable(cacheNames = "links", key = "#code")
    @Transactional(readOnly = true)
    public String longUrlForRedirect(String code) {
        return links.findByCode(code)
                .map(ShortLink::getLongUrl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown code"));
    }

    @Transactional
    public void recordClick(String code) {
        links.incrementClicks(code);
    }

    @Transactional(readOnly = true)
    public LinkStats stats(String code, long requesterId) {
        ShortLink link = links.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown code"));
        if (!link.getOwner().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the owner");
        }
        return new LinkStats(link.getCode(), link.getLongUrl(), link.getClicks());
    }

    public record CreatedLink(String code, String shortUrl) {}

    public record LinkStats(String code, String longUrl, long clicks) {}
}
