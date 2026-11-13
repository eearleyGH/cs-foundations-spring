package com.elishaearley.csfoundations.shortener.link;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {
    Optional<ShortLink> findByCode(String code);

    @Modifying
    @Query("update ShortLink l set l.clicks = l.clicks + 1 where l.code = :code")
    int incrementClicks(@Param("code") String code);
}
