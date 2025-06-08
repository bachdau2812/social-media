package com.dauducbach.identity_service.repository;

import com.dauducbach.identity_service.entity.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RoleRepository extends ReactiveCrudRepository<Role, String> {

}
