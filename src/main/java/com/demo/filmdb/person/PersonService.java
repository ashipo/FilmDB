package com.demo.filmdb.person;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;

    public PersonService(PersonRepository personRepository, RoleRepository roleRepository) {
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Returns a {@link Page} of {@link Person} entities matching the given {@link Specification}.
     * @param spec must not be {@code null}.
     * @param pageable must not be {@code null}.
     * @return a page of filtered entities.
     */
    public Page<Person> search(Specification<Person> spec, Pageable pageable) {
        return personRepository.findAll(spec, pageable);
    }

    /**
     * Returns a {@link Page} of all {@link Person} entities.
     * @param pageable must not be {@code null}.
     * @return a page.
     */
    public Page<Person> getAllPeople(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    /**
     * Returns all the {@link Person} entities with the given ids. If some ids aren't found, throws 404.
     * @param peopleIds ids to find.
     * @return the found entities.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if at least one of the given ids can not be found.
     */
    public List<Person> getPeople(Collection<Long> peopleIds) {
        List<Person> people = personRepository.findAllById(peopleIds);
        if (people.size() < peopleIds.size()) {
            Set<Long> existingIds = people.stream().map(Person::getId).collect(Collectors.toSet());
            List<Long> notFoundIds = new ArrayList<>(peopleIds);
            notFoundIds.removeAll(existingIds);
            throw getNotFoundException(notFoundIds);
        }
        return people;
    }

    /**
     * Saves a given {@link Person} entity.
     * @param person must not be {@code null}.
     * @return the saved entity.
     */
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }

    /**
     * Returns a {@link Person} entity with the given id or throws 404 if it doesn't exist.
     * @param personId must not be {@code null}.
     * @return the found entity.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a person with the given id doesn't exist.
     */
    public Person getPerson(Long personId) {
        return personRepository.findById(personId).orElseThrow(() -> getNotFoundException(personId));
    }

    /**
     * Deletes a {@link Person} entity with the given id or throws 404 if it doesn't exist.
     * @param personId must not be {@code null}.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a person with the given id doesn't exist.
     */
    @Transactional
    public void deletePerson(Long personId) {
        Person person = getPerson(personId);
        person.getFilmsDirected().forEach(f -> f.getDirectors().remove(person));
        roleRepository.deleteById_PersonId(personId);
        personRepository.deleteById(personId);
    }

    /**
     * Returns a collection of films directed by a {@link Person} with the given id.
     * @param personId must not be {@code null}.
     * @return a collection of {@link Film} entities.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a person with the given id doesn't exist.
     */
    public Set<Film> getDirected(Long personId) {
        return getPerson(personId).getFilmsDirected();
    }

    /**
     * Returns a collection of roles acted by a {@link Person} with the given id.
     * @param personId must not be {@code null}.
     * @return a collection of {@link Role} entities.
     * @throws ResponseStatusException {@link HttpStatus#NOT_FOUND} if a person with the given id doesn't exist.
     */
    public Set<Role> getRoles(Long personId) {
        return getPerson(personId).getRoles();
    }

    /**
     * Returns a {@link HttpStatus#NOT_FOUND} exception with a reason containing given {@code id}.
     * @param id to include in the reason.
     * @return {@link ResponseStatusException} with the response status and the reason.
     */
    private ResponseStatusException getNotFoundException(long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find person with id " + id);
    }

    /**
     * Returns a {@link HttpStatus#NOT_FOUND} exception with a reason containing given {@code ids}.
     * @param ids to include in the reason.
     * @return {@link ResponseStatusException} with the response status and the reason.
     */
    private ResponseStatusException getNotFoundException(List<Long> ids) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find people with ids " + ids);
    }
}
