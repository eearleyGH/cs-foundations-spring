package com.elishaearley.csfoundations.shortener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.elishaearley.csfoundations.commons.jwt.JwtTokens;
import com.elishaearley.csfoundations.shortener.auth.AuthService;
import com.elishaearley.csfoundations.shortener.auth.UserAccount;
import com.elishaearley.csfoundations.shortener.auth.UserAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserAccountRepository users;
    @Mock
    PasswordEncoder passwords;
    @Mock
    JwtTokens jwt;
    @InjectMocks
    AuthService auth;

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.findByEmail("ada@example.com")).thenReturn(Optional.of(new UserAccount("ada@example.com", "hash")));

        assertThatThrownBy(() -> auth.register("Ada@example.com", "password1"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verifyNoInteractions(jwt);
    }
}
