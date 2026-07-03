package com.reservation.web;

import com.reservation.web.service.AnnouncementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AnnouncementServiceHtmlSanitizeTests {

    @TempDir
    Path uploadDir;

    @Test
    void keepsUploadedImageSources() {
        AnnouncementService service = new AnnouncementService(null, uploadDir.toString());

        String result = service.sanitizeHtml("<p><img src=\"/uploads/abc123.png\" alt=\"test.PNG\"></p>");

        assertThat(result).contains("src=\"/uploads/abc123.png\"");
        assertThat(result).contains("alt=\"test.PNG\"");
    }

    @Test
    void removesUnsafeImageSources() {
        AnnouncementService service = new AnnouncementService(null, uploadDir.toString());

        String result = service.sanitizeHtml("<p><img src=\"javascript:alert(1)\" alt=\"bad\"></p>");

        assertThat(result).doesNotContain("javascript:");
        assertThat(result).contains("<img alt=\"bad\">");
    }
}
