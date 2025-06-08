package com.dauducbach.profile_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

@Node("user_profile")
public class Profile {
    @Id
    Long id;
    Long userId;
    String username;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    LocalDate dob;

    String city;
    String job;
    String vehicle;

    @JsonIgnore
    @Relationship(type = "FRIEND", direction = Relationship.Direction.OUTGOING)
    List<Profile> friends;

    @JsonIgnore
    @Relationship(type = "FOLLOW", direction = Relationship.Direction.OUTGOING)
    List<Profile> followUser;

    @JsonIgnore
    @Relationship(type = "RESTRICTION", direction = Relationship.Direction.OUTGOING)
    List<Profile> restrictions;

    @JsonIgnore
    @Relationship(type = "BLOCK", direction = Relationship.Direction.OUTGOING)
    List<Profile> blocks;

    @JsonIgnore
    @Relationship(type = "INVITE_ADD", direction = Relationship.Direction.OUTGOING)
    List<Profile> invites;

    List<String> interests;
    List<String> followPage;
}
