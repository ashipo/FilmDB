package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.person.Person;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
     * Creates a {@link Role} with the given parameters. If a role for the given {@code film} and {@code person} already
     * exist, throws 409.
     * @param film must not be {@code null}.
     * @param person must not be {@code null}.
     * @param character name or description of character or characters. Must not be {@code null}.
     * @return the created entity.
     */
    public Role createRole(Film film, Person person, String character) {
        if (roleExists(film.getId(), person.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role for film " + film.getId() +
                    " and person " + person.getId() + " already exists.");
        }
        return saveRole(new Role(film, person, character));
    }

    /**
     * Returns a {@link Role} entity with the given ids or throws 404 if it doesn't exist.
     * @param filmId must not be {@code null}.
     * @param personId must not be {@code null}.
     * @return the found entity.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a role with the given ids doesn't exist.
     */
    public Role getRole(Long filmId, Long personId) {
        return roleRepository.findByIds(filmId, personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find role with filmId " + filmId +
                        " and personId " + personId));
    }

    /**
     * Saves a given {@link Role} entity.
     * @param role must not be {@code null}.
     * @return the saved entity.
     */
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    /**
     * Deletes a {@link Role} entity with the given ids or throws 404 if it doesn't exist.
     * @param filmId must not be {@code null}.
     * @param personId must not be {@code null}.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a role with the given ids doesn't exist.
     */
    public void deleteRole(Long filmId, Long personId) {
        Role role = getRole(filmId, personId);
        roleRepository.delete(role);
    }

    /**
     * Replaces roles for a {@code film} with the given cast.
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
        newCast.forEach((person, character) -> result.add(
                roleRepository.findByIds(film.getId(), person.getId()).
                map(role -> {
                    role.setCharacter(character);
                    return role;
                }).
                orElseGet(() -> saveRole(new Role(film, person, character)))));

        return result;
    }

    /**
     * Returns whether a {@link Role} with the given ids exist.
     * @param filmId must not be {@code null}.
     * @param personId must not be {@code null}.
     * @return true if exists, false otherwise.
     */
    private boolean roleExists(Long filmId, Long personId) {
        RoleKey key = new RoleKey(filmId, personId);
        return roleRepository.existsById(key);
    }
}
