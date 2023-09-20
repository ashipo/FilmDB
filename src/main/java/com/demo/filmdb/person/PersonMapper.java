package com.demo.filmdb.person;

import com.demo.filmdb.person.dtos.PersonDto;
import com.demo.filmdb.person.dtos.PersonDtoInput;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PersonMapper {

    /* PersonDto */

    PersonDto personToPersonDto(Person person);

    /* PersonDtoInput */

    Person personDtoInputToPerson(PersonDtoInput personDtoInput);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Person updatePersonFromPersonDtoInput(PersonDtoInput personDtoInput, @MappingTarget Person person);
}
