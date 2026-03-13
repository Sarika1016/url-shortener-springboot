package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests — loads the full Spring context and tests the
 * complete request-response cycle against the real H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("URL Shortener Integration Tests")
class UrlShortenerIntegrationTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;
    @Autowired private UrlRepository urlRepository;

    @BeforeEach
    void cleanDatabase() {
        urlRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/urls/shorten → 201 with shortUrl in body")
    void shortenUrl_returns201() throws Exception {
        ShortenRequest request = new ShortenRequest("https://www.example.com/some/long/path", null, null);

        MvcResult result = mockMvc.perform(post("/api/urls/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortCode").isNotEmpty())
            .andExpect(jsonPath("$.shortUrl").value(org.hamcrest.Matchers.containsString("localhost:8080")))
            .andExpect(jsonPath("$.originalUrl").value("https://www.example.com/some/long/path"))
            .andReturn();

        String json = result.getResponse().getContentAsString();
        UrlResponse response = objectMapper.readValue(json, UrlResponse.class);
        assertThat(urlRepository.findByShortCode(response.getShortCode())).isPresent();
    }

    @Test
    @DisplayName("POST /api/urls/shorten with blank URL → 400")
    void shortenUrl_blankUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/urls/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("GET /{shortCode} → 302 redirect to original URL")
    void redirect_validCode_returns302() throws Exception {
        // First shorten a URL
        ShortenRequest request = new ShortenRequest("https://www.example.com", null, null);
        MvcResult shortenResult = mockMvc.perform(post("/api/urls/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andReturn();

        UrlResponse response = objectMapper.readValue(
            shortenResult.getResponse().getContentAsString(), UrlResponse.class);

        // Then redirect using it
        mockMvc.perform(get("/" + response.getShortCode()))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "https://www.example.com"));
    }

    @Test
    @DisplayName("GET /{shortCode} for unknown code → 404")
    void redirect_unknownCode_returns404() throws Exception {
        mockMvc.perform(get("/doesnotexist"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/urls/{shortCode}/qr → 200 image/png")
    void getQRCode_returns200Png() throws Exception {
        ShortenRequest request = new ShortenRequest("https://www.example.com", null, null);
        MvcResult shortenResult = mockMvc.perform(post("/api/urls/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andReturn();

        UrlResponse response = objectMapper.readValue(
            shortenResult.getResponse().getContentAsString(), UrlResponse.class);

        mockMvc.perform(get("/api/urls/" + response.getShortCode() + "/qr"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("DELETE /api/urls/{shortCode} → 204, subsequent redirect → 404")
    void deactivate_thenRedirectReturns404() throws Exception {
        ShortenRequest request = new ShortenRequest("https://www.example.com", null, null);
        MvcResult shortenResult = mockMvc.perform(post("/api/urls/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andReturn();

        UrlResponse response = objectMapper.readValue(
            shortenResult.getResponse().getContentAsString(), UrlResponse.class);

        // Deactivate
        mockMvc.perform(delete("/api/urls/" + response.getShortCode()))
            .andExpect(status().isNoContent());

        // Should now 404
        mockMvc.perform(get("/" + response.getShortCode()))
            .andExpect(status().isNotFound());
    }
}
