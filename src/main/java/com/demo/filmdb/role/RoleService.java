package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository repository) {
        this.roleRepository = repository;
    }

    /**
     * Creates a {@link Role}.
     *
     * @param film must not be {@code null}.
     * @param person must not be {@code null}.
     * @param character name or description of character or characters. Must not be {@code null}.
     * @return the created entity.
     */
    public Role createRole(Film film, Person person, String character) {
        return saveRole(new Role(film, person, character));
    }

    /**
     * Returns a {@link Role} entity with the given ids or {@code null} if it doesn't exist.
     *
     * @param filmId must not be {@code null}.
     * @param personId must not be {@code null}.
     * @return the found entity.
     */
    public @Nullable Role getRole(Long filmId, Long personId) {
        return roleRepository.findByIds(filmId, personId).orElse(null);
    }

    /**
     * Saves the given {@link Role} entity.
     *
     * @param role must not be {@code null}.
     * @return the saved entity.
     */
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    /**
     * Deletes the given {@link Role} entity.
     *
     * @param role to delete.
     */
    public void deleteRole(Role role) {
        roleRepository.delete(role);
    }

    /**
     * Replaces roles for the {@code film} with the given cast.
     *
     * @param film must not be {@code null}.
     * @param newCast map of person and character description. Must not be {@code null}.
     * @return the updated cast.
     */
    @Transactional
    public Set<Role> updateCast(Film film, Map<Person, String> newCast) {
        Set<Long> newCastIds = newCast.keySet().stream().map(Person::getId).collect(Collectors.toSet());

        film.getRoles().stream().
                filter(r -> !newCastIds.contains(r.getPerson().getId())).
                forEach(roleRepository::delete);

        Set<Role> result = new HashSet<>();
        newCast.forEach((person, character) -> {
            Role role = roleRepository
                    .findByIds(film.getId(), person.getId())
                    .map(roleToEdit -> {
                        roleToEdit.setCharacter(character);
                        return roleToEdit;
                    })
                    .orElseGet(() -> saveRole(new Role(film, person, character)));
            result.add(role);
        });

        return result;
    }

    /**
     * Returns whether a {@link Role} with the given ids exist.
     *
     * @param filmId must not be {@code null}.
     * @param personId must not be {@code null}.
     * @return true if exists, false otherwise.
     */
    public boolean roleExists(Long filmId, Long personId) {
        RoleKey key = new RoleKey(filmId, personId);
        return roleRepository.findById(key).isPresent();
    }
}
