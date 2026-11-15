package com.inventrack.repository;

import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findByUsernameIgnoreCase(String username);
  boolean existsByUsernameIgnoreCase(String username);
  boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
  boolean existsByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
  long countByStatus(Status status);
  long countByRoleAndStatus(Role role, Status status);
}
