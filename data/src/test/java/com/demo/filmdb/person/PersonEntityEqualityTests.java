package com.demo.filmdb.person;

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
public class PersonEntityEqualityTests {

    @Autowired
    private EntityManagerFactory emf;

    @Test
    @DisplayName("Entity instance equality should be consistent across state changes")
    void entityEqualityTest() {
        Class<Person> entityClass = Person.class;
        Person entity = new Person("Jemaine Clement", LocalDate.of(1974, 1, 10));
        Set<Person> hashed = new HashSet<>();
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
