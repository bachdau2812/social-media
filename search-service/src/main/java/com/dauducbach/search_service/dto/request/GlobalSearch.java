package com.dauducbach.search_service.dto.request;

import com.dauducbach.search_service.dto.response.PostResponse;
import com.dauducbach.search_service.dto.response.ProfileResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class GlobalSearch {
    List<ProfileResponse> listProfile;
    List<PostResponse> listPost;
}
