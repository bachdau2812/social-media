package com.dauducbach.post_service.entity;

import com.dauducbach.post_service.constant.Visibility;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Document("posts")
public class Post {
    @Id
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

    boolean isDelete;

    List<Float> embedding;
}