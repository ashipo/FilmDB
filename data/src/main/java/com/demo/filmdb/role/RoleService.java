package com.demo.filmdb.role;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.film.FilmService;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    @Transactional
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
     * Returns a {@linkplain Role} entity with the given ids or empty {@code Optional} if it doesn't exist
     *
     * @param filmId   must not be {@code null}
     * @param personId must not be {@code null}
     * @return the found entity or empty {@code Optional}
     */
    public Optional<Role> getRole(Long filmId, Long personId) {
        return roleRepository.findById_FilmIdAndId_PersonId(filmId, personId);
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
        Role roleToUpdate = roleRepository.findById_FilmIdAndId_PersonId(filmId, personId).orElseThrow(() ->
                new EntityNotFoundException(roleNotFoundMessage(filmId, personId))
        );
        roleToUpdate.setCharacter(character);
        return roleRepository.save(roleToUpdate);
    }

    /**
     * Deletes a {@link Role} entity for the given ids
     *
     * @param filmId    role film
     * @param personId  role person
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRole(Long filmId, Long personId) {
        Role.Id roleId = new Role.Id(filmId, personId);
        roleRepository.deleteById(roleId);
    }

    /**
     * Returns whether a {@link Role} with the given ids exists
     *
     * @param filmId must not be {@code null}.
     * @param personId must not be {@code null}.
     * @return true if exists, false otherwise.
     */
    public boolean roleExists(Long filmId, Long personId) {
        Role.Id roleId = new Role.Id(filmId, personId);
        return roleRepository.existsById(roleId);
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
            Role roleToUpdate = getRole(filmId, personId).orElse(null);
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
     * Deletes all roles for a {@link Film}
     *
     * @param filmId must not be {@code null}
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCast(Long filmId) {
        roleRepository.deleteById_FilmId(filmId);
    }
}
