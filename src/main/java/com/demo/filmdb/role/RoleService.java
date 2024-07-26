package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.demo.filmdb.util.ErrorUtil.filmNotFoundMessage;
import static com.demo.filmdb.util.ErrorUtil.personNotFoundMessage;

@Service
public class RoleService {

    private PersonService personService;
    private FilmService filmService;

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository repository) {
        this.roleRepository = repository;
    }

    @Autowired
    public void setServices(PersonService personService, FilmService filmService) {
        this.personService = personService;
        this.filmService = filmService;
    }

    /**
     * Creates a {@link Role}
     *
     * @param filmId role {@link Film} id
     * @param personId role {@link Person} id
     * @param character name or description of the character or characters. Must not be empty.
     * @return the created entity
     * @throws EntityNotFoundException if film or person could not be found
     * @throws EntityAlreadyExistsException if role for the given film and person already exists
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Role createRole(Long filmId, Long personId, String character) throws EntityNotFoundException, EntityAlreadyExistsException {
        Film film = filmService.getFilm(filmId);
        if (film == null) {
            throw new EntityNotFoundException(filmNotFoundMessage(filmId));
        }
        Person person = personService.getPerson(personId);
        if (person == null) {
            throw new EntityNotFoundException(personNotFoundMessage(personId));
        }
        if (roleExists(filmId, personId)) {
            throw new EntityAlreadyExistsException("Role for filmId " + filmId + " and personId " + personId + " already exists.");
        }
        return saveRole(new Role(film, person, character));
    }

    /**
     * Returns a {@link Role} entity with the given ids or {@code null} if it doesn't exist
     *
     * @param filmId must not be {@code null}
     * @param personId must not be {@code null}
     * @return the found entity
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
