package com.therdes.app.tinyurl.service.mapping;

import com.therdes.app.tinyurl.bean.mapping.UrlMapping;
import com.therdes.app.tinyurl.repository.mapping.UrlMappingRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UrlMappingService {

    private final char[] availableChar = new char[]{
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '-', '.'
    };

    private final UrlMappingRepository urlMappingRepository;

    @Autowired
    public UrlMappingService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    public String encode(String longUrl) throws NoSuchAlgorithmException {
        List<String> shortUrls = new ArrayList<>();
        byte[] encryptUrl = MessageDigest.getInstance("MD5").digest(longUrl.getBytes(StandardCharsets.UTF_8));
        String md5HashHexString = new BigInteger(encryptUrl).toString(16);
        if (urlMappingRepository.existsByMd5HashHex(md5HashHexString)) {
            List<UrlMapping> mappings = urlMappingRepository.findByMd5HashHex(md5HashHexString);
            for (UrlMapping mapping : mappings) {
                if (StringUtils.equals(mapping.getOriginLongUrl(), longUrl)) {
                    return mapping.getShortUrlHash();
                }
            }
        }

        byte[][] hashs = new byte[4][4];
        for (int i = 0; i < hashs.length; ++i) {
            hashs[i] = Arrays.copyOfRange(encryptUrl, i * 4, (i + 1) * 4);
        }
        long[] finalNumber = new long[4];
        for (int i = 0; i < hashs.length; ++i) {
            finalNumber[i] = new BigInteger(1, hashs[i]).longValue();
        }
        for (long number : finalNumber) {
            long andResult = number & 0x3fffffff;
            String binValue = String.format("%30s", Long.toBinaryString(andResult)).replace(' ', '0');
            final int finalUrlCharAmount = 6;
            String[] splitBinValue = new String[finalUrlCharAmount];
            final int digitAmount = 30 / finalUrlCharAmount;
            for (int i = 0; i < splitBinValue.length; ++i) {
                splitBinValue[i] = binValue.substring(i * digitAmount, (i + 1) * digitAmount);
            }
            String shortUrl = Stream.of(splitBinValue).map(value -> String.valueOf(availableChar[Integer.valueOf(value, 2)]))
                .collect(Collectors.joining());
            shortUrls.add(shortUrl);
        }

        String selectedUrl = null;
        for (String url : shortUrls) {
            if (!urlMappingRepository.existsByShortUrlHash(url)) {
                selectedUrl = url;
                UrlMapping urlMapping = new UrlMapping();
                urlMapping.setShortUrlHash(url);
                urlMapping.setOriginLongUrl(longUrl);
                urlMapping.setMd5HashHex(md5HashHexString);
                urlMapping.setAccessTimes(0L);
                urlMappingRepository.save(urlMapping);
                break;
            }
        }
        return Optional.ofNullable(selectedUrl)
            .orElseThrow(() -> new IllegalArgumentException(String.format("can't simplify the url {%s}, all four hash have been used.", longUrl)));
    }

    public String decode(String shortUrl) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrlHash(shortUrl)
            .orElseThrow(() -> new IllegalArgumentException("No such short url."));
        String originUrl = urlMapping.getOriginLongUrl();

        urlMapping.setAccessTimes(urlMapping.getAccessTimes() + 1L);
        urlMappingRepository.saveAndFlush(urlMapping);

        return originUrl;
    }
}
