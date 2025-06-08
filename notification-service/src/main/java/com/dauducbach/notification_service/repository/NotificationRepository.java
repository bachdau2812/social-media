package com.dauducbach.notification_service.repository;

import com.dauducbach.notification_service.entity.NotificationDB;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<NotificationDB, Long> {
}
