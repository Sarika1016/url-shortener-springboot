package com.urlshortener.controller;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Web Controller for Thymeleaf frontend pages.
 * Different from UrlController which handles REST APIs.
 *
 * @Controller returns HTML pages
 * @RestController returns JSON data
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final UrlService urlService;

    // ── Show Home Page ────────────────────────────────────
    // GET /
    // Opens index.html with empty form
    @GetMapping("/")
    public String home() {
        return "index"; // loads templates/index.html
    }

    // ── Handle Form Submission ────────────────────────────
    // POST /shorten
    // User clicks "Shorten!" button
    @PostMapping("/shorten")
    public String shortenUrl(
            @RequestParam String url,
            Model model) {

        try {
            // Call the service to shorten the URL
            ShortenRequest request = new ShortenRequest(url, null, null);
            UrlResponse response = urlService.shortenUrl(request);

            // Send data to Thymeleaf template
            model.addAttribute("originalUrl", response.getOriginalUrl());
            model.addAttribute("shortUrl",    response.getShortUrl());
            model.addAttribute("shortCode",   response.getShortCode());
            model.addAttribute("clickCount",  response.getClickCount());
            model.addAttribute("createdAt",   response.getCreatedAt()
                                              .toLocalDate().toString());

        } catch (Exception e) {
            // Send error message to template
            model.addAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("originalUrl", url);
        }

        return "index"; // reload index.html with results
    }
}