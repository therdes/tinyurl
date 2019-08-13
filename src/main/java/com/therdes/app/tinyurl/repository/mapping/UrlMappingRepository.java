package com.therdes.app.tinyurl.repository.mapping;

import com.therdes.app.tinyurl.bean.mapping.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortUrlHash(String shortUrlHash);

    boolean existsByShortUrlHash(String shortUrlHash);

    List<UrlMapping> findByMd5HashHex(String md5HashHex);

    boolean existsByMd5HashHex(String md5HashHex);
}
