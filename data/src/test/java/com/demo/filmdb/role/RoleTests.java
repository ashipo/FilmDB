package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.demo.filmdb.util.Creators.createFilm;
import static com.demo.filmdb.util.Creators.createPerson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Role")
public class RoleTests {

    @Nested
    class equals {

        @Test
        @DisplayName("Roles with the same Film and Person are equal")
        void sameFilmAndRole_equal() {
            Film film = createFilm(1L);
            Person person = createPerson(10L);
            String character = "Marty McFly";

            Role role1 = new Role(film, person, character);
            Role role2 = new Role(film, person, character);
            Role role3 = new Role(film, person, "George McFly");

            assertEquals(role1, role2);
            assertEquals(role1, role3);
        }

        @Test
        @DisplayName("Roles with different Film or Person aren't equal")
        void differentFilmAndRole_NotEqual() {
            Film film1 = new Film(1L, "Back to the Future", LocalDate.of(1985, 7, 3), "Marty McFly");
            Film film2 = new Film(2L, "Back to the Future Part II", LocalDate.of(1989, 11, 20), "After visiting 2015");
            Person person1 = new Person(10L, "Tom Wilson", null);
            Person person2 = new Person(11L, "Christopher Lloyd", null);
            String character = "Dr. Emmett Brown";

            Role role1 = new Role(film1, person1, character);
            Role role2 = new Role(film2, person1, character);
            Role role3 = new Role(film2, person2, character);

            assertNotEquals(role1, role2);
            assertNotEquals(role2, role3);
        }
    }
}
