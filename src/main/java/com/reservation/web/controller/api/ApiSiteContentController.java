package com.reservation.web.controller.api;

import com.reservation.web.dto.SiteContentDTO;
import com.reservation.web.service.SiteContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiSiteContentController {

    private final SiteContentService siteContentService;

    @GetMapping("/site-content")
    public ApiResponse<SiteContentDTO> getSiteContent() {
        return ApiResponse.ok(siteContentService.getContent());
    }

    @PutMapping(value = "/admin/site-content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SiteContentDTO> updateSiteContent(@RequestPart("content") SiteContentDTO content,
                                                         @RequestPart(name = "heroImage", required = false) MultipartFile heroImage) throws IOException {
        return ApiResponse.ok(siteContentService.updateContent(content, heroImage), "메인 화면 콘텐츠가 저장되었습니다.");
    }
}
