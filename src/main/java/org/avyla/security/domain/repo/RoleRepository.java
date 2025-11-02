package org.avyla.security.domain.repo;

import org.avyla.security.domain.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long>
{
    List<RoleEntity> findByRoleEnumIn(List<String> roleName);
}
