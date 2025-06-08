package com.dauducbach.feed_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class PostResponse {
    String id;
    String userId;
    String content;
    List<String> mediaUrls;     // Danh sách ảnh hoặc video (có thể dùng CDN, Cloudinary, S3,...)

    Instant createAt;
    Instant updateAt;

    Visibility visibility;
    List<String> tags;          // Các hashtag, sở thích, chủ đề
    List<Float> location;
    String geoLocation;

    int likeCount;
    int commentCount;
    int shareCount;
}