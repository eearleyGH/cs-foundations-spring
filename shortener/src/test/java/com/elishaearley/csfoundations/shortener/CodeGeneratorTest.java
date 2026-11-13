package com.elishaearley.csfoundations.shortener;

import static org.assertj.core.api.Assertions.assertThat;

import com.elishaearley.csfoundations.shortener.link.CodeGenerator;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CodeGeneratorTest {
    private final CodeGenerator codes = new CodeGenerator();

    @Test
    void nextIsSevenUrlSafeCharacters() {
        String code = codes.next();
        assertThat(code).hasSize(7).matches("[A-Za-z0-9]{7}");
    }

    @Test
    void nextIsNotConstant() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            seen.add(codes.next());
        }
        assertThat(seen.size()).isGreaterThan(1);
    }
}
