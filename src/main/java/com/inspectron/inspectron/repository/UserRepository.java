package com.inspectron.inspectron.repository;

import com.inspectron.inspectron.domain.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEnrollmentAndDisabledFalse(String enrollment);

    Optional<User> findByEnrollment(String enrollment);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByEnrollment(String enrollment);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByEnrollmentAndIdNot(String enrollment, UUID id);
}
