package com.dauducbach.chat_service.controller;

import com.dauducbach.chat_service.service.UploadFileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

@RequestMapping("/upload")
public class UploadFileController {
    UploadFileService uploadFileService;

    @PostMapping
    Mono<String> upload(@RequestPart("file") FilePart file) throws IOException {
        return uploadFileService.upload(file);
    }
}
