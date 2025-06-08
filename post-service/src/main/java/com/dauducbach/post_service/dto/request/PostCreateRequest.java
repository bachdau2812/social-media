package com.dauducbach.post_service.dto.request;

import com.dauducbach.post_service.constant.Visibility;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCreateRequest {
    String userId;
    String content;
    List<String> mediaUrls;
    Visibility visibility;
    List<String> tags;
    List<Float> location;
}