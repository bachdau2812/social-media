package com.dauducbach.chat_service.repository;

import com.dauducbach.chat_service.entity.Group;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends ReactiveMongoRepository<Group, String> {

}