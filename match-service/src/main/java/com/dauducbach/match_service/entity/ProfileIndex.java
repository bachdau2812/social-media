package com.dauducbach.match_service.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

@Document(indexName = "profiles")
public class ProfileIndex {
    @Id
    String id;
    String userId;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    String username;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    String city;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    String job;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    String vehicle;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    List<String> interests;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    List<String> followPage;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    List<String> followUser;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    List<String> friends;

    List<Float> embeddings;
}
