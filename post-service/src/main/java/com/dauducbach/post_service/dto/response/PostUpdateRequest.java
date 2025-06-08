package com.dauducbach.post_service.dto.response;

import com.dauducbach.post_service.constant.Visibility;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostUpdateRequest {
    String content;
    List<String> mediaUrls;
    Visibility visibility;
    List<String> tags;
}