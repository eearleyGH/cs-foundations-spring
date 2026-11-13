package com.elishaearley.csfoundations.shortener;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elishaearley.csfoundations.shortener.config.SecurityConfig;
import com.elishaearley.csfoundations.shortener.link.LinkService;
import com.elishaearley.csfoundations.shortener.web.LinkController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LinkController.class)
@Import(SecurityConfig.class)
class LinkControllerContractTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    LinkService links;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void createRequiresJwt() throws Exception {
        mvc.perform(post("/api/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\":\"https://example.com/a\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturnsCode() throws Exception {
        when(links.create(eq(9L), eq("https://example.com/a")))
                .thenReturn(new LinkService.CreatedLink("Ab3DeFg", "http://localhost:8080/Ab3DeFg"));

        mvc.perform(post("/api/codes")
                        .with(jwt().jwt(token -> token.subject("9")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\":\"https://example.com/a\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("Ab3DeFg"));
    }
}
