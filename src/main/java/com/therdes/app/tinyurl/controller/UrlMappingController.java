package com.therdes.app.tinyurl.controller;

import com.therdes.app.tinyurl.service.mapping.UrlMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping("/")
public class UrlMappingController {

    private final UrlMappingService urlMappingService;

    @Autowired
    public UrlMappingController(UrlMappingService urlMappingService) {
        this.urlMappingService = urlMappingService;
    }

    @RequestMapping("/")
    @ResponseBody
    public String index() {
        return "Welcome to use TinyUrl Service!";
    }

    @RequestMapping(value = "/generate", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String generateShortHash(HttpServletRequest request, @RequestParam(value = "longUrl") String longUrl) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String originUrl = URLDecoder.decode(longUrl, StandardCharsets.UTF_8.displayName());
        String shortUrl = urlMappingService.encode(originUrl);
        return request.getScheme() + "://" + request.getRemoteHost() + request.getContextPath() + "/" + shortUrl;
    }

    @GetMapping("/{shortHash}")
    public void shortToLong(HttpServletResponse response, @PathVariable("shortHash") String shortHash) throws IOException {
        String originUrl = urlMappingService.decode(shortHash);
        response.sendRedirect(originUrl);
    }
}
