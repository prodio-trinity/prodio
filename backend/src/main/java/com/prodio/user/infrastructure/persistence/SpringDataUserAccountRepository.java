package com.prodio.user.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.prodio.user.UserRole;
import com.prodio.user.domain.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataUserAccountRepository extends JpaRepository<UserAccountEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserAccountEntity> findByEmailIgnoreCase(String email);

    Optional<UserAccountEntity> findByEmailIgnoreCaseAndStatus(String email, UserAccount.Status status);

    List<UserAccountEntity> findByIdInAndStatus(Collection<Long> ids, UserAccount.Status status);

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT u FROM UserAccountEntity u WHERE u.id = :userId")
    Optional<UserAccountEntity> findWithRolesById(@Param("userId") long userId);

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT u FROM UserAccountEntity u WHERE u.email = :email")
    Optional<UserAccountEntity> findWithRolesByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT u FROM UserAccountEntity u WHERE u.id = :userId AND u.status = :status")
    Optional<UserAccountEntity> findWithRolesByIdAndStatus(
            @Param("userId") long userId,
            @Param("status") UserAccount.Status status);

    @EntityGraph(attributePaths = "roles")
    @Query("SELECT DISTINCT u FROM UserAccountEntity u WHERE u.id IN :userIds")
    List<UserAccountEntity> findAllWithRolesByIdIn(@Param("userIds") Collection<Long> userIds);

    @Query("""
            SELECT u FROM UserAccountEntity u
            WHERE u.status = :status
              AND (:term = ''
                   OR LOWER(COALESCE(u.name, '')) LIKE CONCAT('%', :term, '%')
                   OR LOWER(u.email) LIKE CONCAT('%', :term, '%'))
            ORDER BY LOWER(COALESCE(NULLIF(u.name, ''), u.email)), u.id
            """)
    List<UserAccountEntity> searchByStatus(
            @Param("status") UserAccount.Status status,
            @Param("term") String term,
            Pageable pageable);

    @Query(value = """
            SELECT u FROM UserAccountEntity u
            WHERE :term = ''
               OR LOWER(COALESCE(u.name, '')) LIKE CONCAT('%', :term, '%')
               OR LOWER(u.email) LIKE CONCAT('%', :term, '%')
            ORDER BY LOWER(COALESCE(NULLIF(u.name, ''), u.email)), u.id
            """, countQuery = """
            SELECT COUNT(u) FROM UserAccountEntity u
            WHERE :term = ''
               OR LOWER(COALESCE(u.name, '')) LIKE CONCAT('%', :term, '%')
               OR LOWER(u.email) LIKE CONCAT('%', :term, '%')
            """)
    Page<UserAccountEntity> searchAll(@Param("term") String term, Pageable pageable);

    @Query("""
            SELECT DISTINCT u.id FROM UserAccountEntity u
            JOIN u.roles r
            WHERE u.status = :status AND r.code = :role
            ORDER BY u.id
            """)
    List<Long> findIdsByStatusAndRole(
            @Param("status") UserAccount.Status status,
            @Param("role") UserRole role);
}
