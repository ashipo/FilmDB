package com.demo.filmdb.person;

import com.demo.filmdb.person.dtos.PersonDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PersonDtoMapper {

    PersonDto personToPersonDto(Person person);
}
