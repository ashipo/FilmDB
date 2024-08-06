package com.demo.filmdb.person;

import com.demo.filmdb.role.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
     *
     * @param spec must not be {@code null}.
     * @param pageable must not be {@code null}.
     * @return a page of filtered entities.
     */
    public Page<Person> search(Specification<Person> spec, Pageable pageable) {
        return personRepository.findAll(spec, pageable);
    }

    /**
     * Returns a {@link Page} of all {@link Person} entities.
     *
     * @param pageable must not be {@code null}.
     * @return a page.
     */
    public Page<Person> getAllPeople(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    /**
     * Returns all the {@link Person} entities found for the given ids. Returned collection can be smaller than
     * {@code peopleIds}.
     *
     * @param peopleIds ids to find.
     * @return the found entities.
     */
    public List<Person> getPeople(Collection<Long> peopleIds) {
        return personRepository.findAllById(peopleIds);
    }

    /**
     * Saves the given {@link Person} entity.
     *
     * @param person must not be {@code null}.
     * @return the saved entity.
     */
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }

    /**
     * Returns a {@linkplain Person} entity with the given id or empty {@code Optional} if it doesn't exist
     *
     * @param personId must not be {@code null}
     * @return the found entity or empty {@code Optional}
     */
    public Optional<Person> getPerson(Long personId) {
        return personRepository.findById(personId);
    }

    /**
     * Deletes the given {@link Person} entity.
     *
     * @param person to delete.
     */
    @Transactional
    public void deletePerson(Person person) {
        person.getFilmsDirected().forEach(f -> f.getDirectors().remove(person));
        roleRepository.deleteById_PersonId(person.getId());
        personRepository.delete(person);
    }
}
