package org.avyla.security.domain.repo;

import org.avyla.security.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findUserEntitiesByUsername(String username);

    @Query("SELECT u.userId FROM UserEntity u WHERE u.username = :username")
    Optional<Long> findUserIdByUsername(String username);

}
