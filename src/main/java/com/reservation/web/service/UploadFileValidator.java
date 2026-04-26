package com.reservation.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

final class UploadFileValidator {

    private static final int SIGNATURE_BUFFER_SIZE = 16;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private UploadFileValidator() {
    }

    static void validateImage(MultipartFile file, String originalFileName) throws IOException {
        String extension = extensionOf(originalFileName);
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new IOException("이미지 파일만 업로드할 수 있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IOException("허용되지 않는 이미지 형식입니다.");
        }

        byte[] signature = readSignature(file);
        if (!matchesImageSignature(extension, signature)) {
            throw new IOException("파일 내용이 이미지 형식과 일치하지 않습니다.");
        }
    }

    static void validateAttachment(MultipartFile file, String originalFileName, Set<String> allowedExtensions) throws IOException {
        String extension = extensionOf(originalFileName);
        if (!allowedExtensions.contains(extension)) {
            throw new IOException("허용되지 않는 파일 형식입니다.");
        }

        byte[] signature = readSignature(file);
        boolean valid = switch (extension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> matchesImageSignature(extension, signature);
            case "pdf" -> startsWith(signature, 0x25, 0x50, 0x44, 0x46);
            case "doc", "hwp" -> startsWith(signature, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
            case "docx", "hwpx" -> startsWith(signature, 0x50, 0x4B, 0x03, 0x04)
                    || startsWith(signature, 0x50, 0x4B, 0x05, 0x06)
                    || startsWith(signature, 0x50, 0x4B, 0x07, 0x08);
            case "txt" -> !containsNullByte(signature);
            default -> false;
        };

        if (!valid) {
            throw new IOException("파일 내용이 확장자와 일치하지 않습니다.");
        }
    }

    static String extensionOf(String originalFileName) throws IOException {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IOException("파일 이름이 비어 있습니다.");
        }

        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == originalFileName.length() - 1) {
            throw new IOException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        return originalFileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private static byte[] readSignature(MultipartFile file) throws IOException {
        byte[] buffer = new byte[SIGNATURE_BUFFER_SIZE];
        try (InputStream inputStream = file.getInputStream()) {
            int read = inputStream.read(buffer);
            if (read <= 0) {
                throw new IOException("파일 내용을 읽을 수 없습니다.");
            }
            if (read == buffer.length) {
                return buffer;
            }

            byte[] truncated = new byte[read];
            System.arraycopy(buffer, 0, truncated, 0, read);
            return truncated;
        }
    }

    private static boolean matchesImageSignature(String extension, byte[] signature) {
        return switch (extension) {
            case "jpg", "jpeg" -> startsWith(signature, 0xFF, 0xD8, 0xFF);
            case "png" -> startsWith(signature, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif" -> startsWith(signature, 0x47, 0x49, 0x46, 0x38);
            case "webp" -> startsWith(signature, 0x52, 0x49, 0x46, 0x46)
                    && signature.length >= 12
                    && signature[8] == 0x57
                    && signature[9] == 0x45
                    && signature[10] == 0x42
                    && signature[11] == 0x50;
            default -> false;
        };
    }

    private static boolean startsWith(byte[] actual, int... expected) {
        if (actual.length < expected.length) {
            return false;
        }

        for (int i = 0; i < expected.length; i++) {
            if ((actual[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsNullByte(byte[] signature) {
        for (byte value : signature) {
            if (value == 0) {
                return true;
            }
        }
        return false;
    }
}
