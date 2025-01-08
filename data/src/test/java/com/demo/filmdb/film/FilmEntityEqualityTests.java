package com.demo.filmdb.film;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCollection;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FilmEntityEqualityTests {

    @Autowired
    private EntityManagerFactory emf;

    @Test
    @DisplayName("Entity instance equality should be consistent across state changes")
    void entityEqualityTest() {
        Class<Film> entityClass = Film.class;
        Film entity = new Film("What We Do in the Shadows", LocalDate.of(2014, 1, 19), "Viago, Deacon, and Vladislav are vampires");
        Set<Film> hashed = new HashSet<>();
        hashed.add(entity);

        assertThatCollection(hashed).withFailMessage("Transient entity is not found").contains(entity);

        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.persist(entity);

            assertThatCollection(hashed).withFailMessage("Persistent entity is not found").contains(entity);
            em.getTransaction().commit();
        }

        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();

            var fetched = em.find(entityClass, entity.getId());

            assertThatCollection(hashed).withFailMessage("Persistent in a new persistence context entity is not found").contains(fetched);

            em.detach(fetched);
            assertThatCollection(hashed).withFailMessage("Detached entity is not found").contains(fetched);

            em.getTransaction().commit();
        }

        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();

            var removed = em.find(entityClass, entity.getId());
            em.remove(removed);

            assertThatCollection(hashed).withFailMessage("Removed entity is not found").contains(removed);

            em.getTransaction().commit();
        }
    }
}
