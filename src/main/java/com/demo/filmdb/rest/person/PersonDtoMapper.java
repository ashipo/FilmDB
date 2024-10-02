package com.demo.filmdb.rest.person;

import com.demo.filmdb.person.Person;
import com.demo.filmdb.rest.person.dtos.PersonDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PersonDtoMapper {

    PersonDto personToPersonDto(Person person);
}
