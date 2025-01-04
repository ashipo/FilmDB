package com.demo.filmdb.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Role.Id> {

    @Transactional
    void deleteById_FilmId(Long filmId);

    @Transactional
    void deleteById_PersonId(Long personId);

    /**
     * Finds a {@link Role} entity by a film id and a person id
     *
     * @param filmId must not be {@code null}
     * @param personId must not be {@code null}
     * @return the entity with the given ids or {@link Optional#empty} if none found
     */
    @Query("SELECT r FROM Role r WHERE r.id.filmId = ?1 AND r.id.personId = ?2")
    Optional<Role> findByIds(Long filmId, Long personId);
}
