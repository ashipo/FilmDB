package com.demo.filmdb.person;

import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleRepository;
import com.demo.filmdb.util.EntityNotFoundException;
import com.demo.filmdb.util.SortUtil;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static com.demo.filmdb.util.ErrorUtil.personNotFoundMessage;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PersonInfoMapper personMapper;
    private final PersonSpecs personSpecs;

    public PersonService(PersonRepository personRepository, RoleRepository roleRepository, PersonInfoMapper personMapper, PersonSpecs personSpecs) {
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.personMapper = personMapper;
        this.personSpecs = personSpecs;
    }

    /**
     * Returns a {@link Page} of all {@link Person} entities
     *
     * @param pageable must not be null
     * @return the resulting page, may be empty but not null
     */
    public Page<Person> getPeople(Pageable pageable) {
        Pageable filteredPageable = SortUtil.filterSortableFields(pageable, Person.class);
        return personRepository.findAll(filteredPageable);
    }

    /**
     * Returns a {@link Page} of {@link Person} entities.
     * For filtering any of {@code name}, {@code bornAfter} or {@code bornBefore} can be specified.
     *
     * @param pageable    must not be null
     * @param name        string that person name must contain. Should not be blank. Can be null.
     * @param bornAfter   birthdate lower limit. Can be null.
     * @param bornBefore  birthdate upper limit. Can be null.
     * @return the resulting page, may be empty but not null
     */
    public Page<Person> getPeople(
            Pageable pageable,
            @Nullable String name,
            @Nullable LocalDate bornAfter,
            @Nullable LocalDate bornBefore
    ) {
        Specification<Person> spec = personSpecs.nameContains(name)
                .and(personSpecs.bornAfter(bornAfter))
                .and(personSpecs.bornBefore(bornBefore));
        Pageable filteredPageable = SortUtil.filterSortableFields(pageable, Person.class);
        return personRepository.findAll(spec, filteredPageable);
    }

    /**
     * Returns a {@link Page} of {@link Person} entities.
     * For filtering any of {@code name}, {@code bornAfter} or {@code bornBefore} can be specified.
     *
     * @param page          page number
     * @param pageSize      page size
     * @param sortBy        {@link Person} property name to sort by. Can be null.
     * @param sortDirection sort direction. Can be null.
     * @param name          string that person name must contain. Should not be blank. Can be null.
     * @param bornAfter     birthdate lower limit. Can be null.
     * @param bornBefore    birthdate upper limit. Can be null.
     * @return the resulting page, may be empty but not null
     */
    public Page<Person> getPeople(
            int page,
            int pageSize,
            @Nullable String sortBy,
            @Nullable Sort.Direction sortDirection,
            @Nullable String name,
            @Nullable LocalDate bornAfter,
            @Nullable LocalDate bornBefore
    ) {
        Pageable pageable;
        if (sortBy == null || sortDirection == null) {
            pageable = PageRequest.of(page, pageSize);
        } else {
            pageable = PageRequest.of(page, pageSize, sortDirection, sortBy);
        }
        return getPeople(pageable, name, bornAfter, bornBefore);
    }

    /**
     * Create a {@link Person}
     *
     * @param personInfo person info
     * @return created entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Person createPerson(PersonInfo personInfo) {
        final Person person = personMapper.personInfoToPerson(personInfo);
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
     * Update a {@link Person}
     *
     * @param personId person to update
     * @param personInfo person info
     * @return the updated entity
     * @throws EntityNotFoundException if person could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Person updatePerson(Long personId, PersonInfo personInfo) throws EntityNotFoundException {
        Person personToUpdate = personRepository.findById(personId).orElseThrow(() ->
                new EntityNotFoundException(personNotFoundMessage(personId))
        );
        personMapper.updatePersonFromPersonInfo(personInfo, personToUpdate);
        return personRepository.save(personToUpdate);
    }

    /**
     * Deletes the {@link Person} with the given id
     *
     * @param personId person id
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePerson(Long personId) {
        getPerson(personId).ifPresent(person -> {
            person.removeFilmsDirected();
            roleRepository.deleteById_PersonId(personId);
            personRepository.deleteById(personId);
        });
    }

    /**
     * Returns whether a {@link Person} with the given id exists
     *
     * @param personId must not be {@code null}
     * @return true if exists, false otherwise
     */
    public boolean personExists(Long personId) {
        return personRepository.existsById(personId);
    }

    /**
     * Returns roles of a person
     *
     * @param personId must not be null
     * @return collection of roles
     * @throws EntityNotFoundException if person could not be found
     */
    public Collection<Role> getRoles(Long personId) throws EntityNotFoundException {
        Person person = personRepository.findById(personId).orElseThrow(() ->
                new EntityNotFoundException(personNotFoundMessage(personId))
        );
        return person.getRoles();
    }
}
