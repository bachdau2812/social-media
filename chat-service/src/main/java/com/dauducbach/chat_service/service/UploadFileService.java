package com.dauducbach.chat_service.service;

import com.cloudinary.Cloudinary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service

public class UploadFileService {
    Cloudinary cloudinary;

    public Mono<String> upload(FilePart filePart) {
        return Mono.fromCallable(() -> {
                    File tempFile = File.createTempFile("upload-", filePart.filename());
                    return tempFile;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tempFile ->
                        filePart.transferTo(tempFile.toPath())
                                .then(Mono.fromCallable(() -> {
                                    Map<?, ?> uploadResult = cloudinary.uploader().upload(tempFile, Map.of(
                                            "resource_type", "auto",
                                            "folder", "chat-uploads"
                                    ));
                                    tempFile.delete();
                                    return uploadResult.get("secure_url").toString();
                                }))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .doOnError(e -> log.error("Upload failed: ", e));
    }


}