package com.therdes.app.tinyurl.bean.mapping;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "short_url_hash", length = 10, unique = true, nullable = false)
    private String shortUrlHash;

    @Column(name = "origin_long_url", length = 500, nullable = false)
    private String originLongUrl;

    @Column(name = "md5_hash_hex", length = 32, nullable = false)
    private String md5HashHex;

    @Column(name = "access_times")
    private Long accessTimes;
}
