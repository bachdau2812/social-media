package com.dauducbach.identity_service.repository;

import com.dauducbach.identity_service.entity.Permission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends ReactiveCrudRepository<Permission, String> {

}
