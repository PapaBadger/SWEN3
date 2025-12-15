package org.swen.dms.helper;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GenerateFileKeyTest {

    @Test
    void generateFileKey_ReturnsValidFormat() {
        GenerateFileKey generator = new GenerateFileKey();

        String result = generator.generateFileKey();

        // 1. Verify it is not null
        assertThat(result).isNotNull();

        // 2. Verify the suffix (crucial for file handling later)
        assertThat(result).endsWith(".pdf");

        // 3. Verify the length (UUID is 36 chars + ".pdf" (4 chars) = 40 chars)
        assertThat(result).hasSize(40);
    }
}