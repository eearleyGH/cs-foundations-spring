package com.elishaearley.csfoundations.shortener.link;

import com.elishaearley.csfoundations.shortener.auth.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "short_links")
public class ShortLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "long_url", nullable = false)
    private String longUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

    @Column(nullable = false)
    private long clicks;

    public ShortLink() {}

    public ShortLink(String code, String longUrl, UserAccount owner) {
        this.code = code;
        this.longUrl = longUrl;
        this.owner = owner;
        this.clicks = 0;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public long getClicks() {
        return clicks;
    }
}
