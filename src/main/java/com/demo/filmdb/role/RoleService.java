package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.role.dtos.CastMember;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.demo.filmdb.util.ErrorUtil.*;

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
        Film film = filmService.getFilm(filmId).orElseThrow(() ->
                new EntityNotFoundException(filmNotFoundMessage(filmId))
        );
        Person person = personService.getPerson(personId).orElseThrow(() ->
                new EntityNotFoundException(personNotFoundMessage(personId))
        );
        if (roleExists(filmId, personId)) {
            throw new EntityAlreadyExistsException("Role for filmId " + filmId + " and personId " + personId + " already exists.");
        }
        return roleRepository.save(new Role(film, person, character));
    }

    /**
     * Returns a {@link Role} entity with the given ids or {@code null} if it doesn't exist
     *
     * @param filmId   must not be {@code null}
     * @param personId must not be {@code null}
     * @return the found entity
     */
    public @Nullable Role getRole(Long filmId, Long personId) {
        return roleRepository.findByIds(filmId, personId).orElse(null);
    }

    /**
     * Updates the {@link Role} for the given ids
     *
     * @param filmId    role film
     * @param personId  role person
     * @param character role character
     * @return the updated entity
     * @throws EntityNotFoundException if role could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Role updateRole(Long filmId, Long personId, String character) throws EntityNotFoundException {
        Role roleToUpdate = roleRepository.findByIds(filmId, personId)
                .orElseThrow(() -> new EntityNotFoundException(roleNotFoundMessage(filmId, personId)));
        roleToUpdate.setCharacter(character);
        return roleRepository.save(roleToUpdate);
    }

    /**
     * Replaces {@linkplain Film} cast
     *
     * @param filmId film id
     * @param cast new cast
     * @return updated cast
     * @throws EntityNotFoundException if film or person could not be found
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<Role> updateCast(Long filmId, @Nullable List<? extends CastMember> cast) throws EntityNotFoundException {
        Film film = filmService.getFilm(filmId).orElseThrow(() ->
                new EntityNotFoundException(filmNotFoundMessage(filmId))
        );
        // If new cast is null or empty, delete all roles for the film
        if (cast == null || cast.isEmpty()) {
            film.getCast().forEach(roleRepository::delete);
            return Collections.emptyList();
        }
        // Remove roles that don't exist anymore
        Set<Long> newCastIds = cast.stream().map(CastMember::getPersonId).collect(Collectors.toSet());
        film.getCast().stream().
                filter(role -> !newCastIds.contains(role.getPerson().getId())).
                forEach(roleRepository::delete);
        // Update existing and create new roles
        List<Role> updatedCast = new ArrayList<>();
        cast.forEach(role -> {
            final Long personId = role.getPersonId();
            Role roleToUpdate = getRole(filmId, personId);
            Role updatedRole;
            if (roleToUpdate == null) {
                Person person = personService.getPerson(personId).orElseThrow(() ->
                        new EntityNotFoundException(personNotFoundMessage(personId))
                );
                updatedRole = roleRepository.save(new Role(film, person, role.getCharacter()));
            } else {
                roleToUpdate.setCharacter(role.getCharacter());
                updatedRole = roleRepository.save(roleToUpdate);
            }
            updatedCast.add(updatedRole);
        });
        return updatedCast;
    }

    /**
     * Deletes a {@link Role} entity for the given ids
     *
     * @param filmId    role film
     * @param personId  role person
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRole(Long filmId, Long personId) {
        RoleKey roleKey = new RoleKey(filmId, personId);
        roleRepository.deleteById(roleKey);
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
