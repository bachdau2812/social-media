package com.dauducbach.profile_service.repository;

import com.dauducbach.profile_service.entity.Profile;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ProfileRepository extends ReactiveNeo4jRepository<Profile, Long> {
    Mono<Profile> findByUserId(Long userId);
    Mono<Void> deleteByUserId(Long userId);

    @Query("""
        MATCH (a:user_profile {userId: $fromUserId}), (b:user_profile {userId: $toUserId})
        WHERE NOT EXISTS ((b)-[:BLOCK]->(a))
            AND NOT EXISTS ((a)-[:FRIEND]->(b))
            AND NOT EXISTS ((a)-[:INVITE_ADD]->(b))
        CREATE (a)-[r:INVITE_ADD {sentAt: datetime()}]->(b)
        RETURN "invite complete"
        """)
    Mono<String> addFriend(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
        MATCH (a:user_profile {userId: $fromUserId}), (b:user_profile {userId: $toUserId})
        MATCH (b)-[r:INVITE_ADD]->(a)
        DELETE r
        CREATE (a)-[:FRIEND {since: datetime()}]->(b)
        CREATE (b)-[:FRIEND {since: datetime()}]->(a)
        RETURN "accept complete, now already friend"
        """)
    Mono<String> acceptFriend(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (a:user_profile {userId: $fromUserId})-[r:FRIEND]-(b:user_profile {userId: $toUserId})
            DELETE r
            WITH COUNT(r) AS deletedCount
            RETURN
                CASE
                    WHEN deletedCount > 0 THEN 'Unfriend complete'
                    ELSE 'Not friend yet'
                END AS result
            """)
    Mono<String> deleteFriend(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (a:user_profile {userId: $fromUserId}), (b:user_profile {userId: $toUserId})
            WHERE NOT EXISTS((b)-[:BLOCK]->(a))
                AND NOT EXISTS((a)-[:BLOCK]->(b))

            FOREACH (r IN [(a)-[rel:FRIEND]-(b) | rel] | DELETE r)
            FOREACH (r IN [(a)-[rel:FRIEND_REQUEST]-(b) | rel] | DELETE r)
            
            CREATE (a)-[:BLOCK {blockAt: datetime()}]->(b)
            RETURN "block complete"
            """)
    Mono<String> blockUser(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (a:user_profile {userId: $fromUserId})-[r:BLOCK]->(b:user_profile {userId: $toUserId})
            DELETE r
            RETURN "unblock complete"
            """)
    Mono<String> unBlockUser(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (a:user_profile {userId: $fromUserId}), (b:user_profile {userId: $toUserId})
            WHERE NOT EXISTS((a)-[:RESTRICTION]->(b))
            CREATE (a)-[:RESTRICTION {restrictionAt: datetime()}]->(b)
            RETURN "restriction compete"
            """)
    Mono<String> restrictionUser(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (a:user_profile {userId: $fromUserId})-[r:RESTRICTION]->(b:user_profile {userId: $toUserId})
            DELETE r
            RETURN "unRestriction compete"
            """)
    Mono<String> unRestrictionUser(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (a:user_profile {userId: $fromUserId}), (b:user_profile {userId: $toUserId})
            WHERE NOT EXISTS((a)-[:FOLLOW]->(b))
                AND NOT EXISTS((b)-[:BLOCK]->(a))
            CREATE (a)-[:FOLLOW {followAt: datetime()}]->(b)
            RETURN "follow compete"
            """)
    Mono<String> followUser(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (a:user_profile {userId: $fromUserId})-[r:FOLLOW]->(b:user_profile {userId: $toUserId})
            DELETE r
            RETURN "unFollow compete"
            """)
    Mono<String> unFollowUser(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId
    );

    @Query("""
            MATCH (requester:user_profile)-[:INVITE_ADD]->(target:user_profile {userId: $userId})
            RETURN requester
            """)
    Flux<Profile> findAllInvitationsByUserId(@Param("userId") Long userId);
}
