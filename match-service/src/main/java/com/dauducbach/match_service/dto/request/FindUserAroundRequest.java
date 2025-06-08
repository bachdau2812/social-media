package com.dauducbach.match_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FindUserAroundRequest {
    double latitude;
    double longitude;
    double radiusKm;       // Bán kính tìm kiếm (tính bằng km)
    int limit;             // Số lượng người dùng tối đa cần trả về (tùy chọn)
}