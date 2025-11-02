package org.avyla.security.domain.repo;

import org.avyla.security.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>
{
    List<Role> findByRoleEnumIn(List<String> roleName);
}
