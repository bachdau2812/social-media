package com.dauducbach.post_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Document(indexName = "posts")
public class PostIndex {
    @Id
    String id;
    String userId;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    String content;
    List<String> mediaUrls;     // Danh sách ảnh hoặc video (có thể dùng CDN, Cloudinary, S3,...)

    @Field(type = FieldType.Date, pattern = "epoch_millis")
    Instant createAt;

    @Field(type = FieldType.Date, pattern = "epoch_millis")
    Instant updateAt;
    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    List<String> tags;          // Các hashtag, sở thích, chủ đề

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    List<Float> location;
    String geoLocation;

    int likeCount;
    int commentCount;
    int shareCount;

    List<Float> embedding;
}