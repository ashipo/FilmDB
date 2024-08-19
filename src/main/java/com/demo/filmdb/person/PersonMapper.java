package com.demo.filmdb.person;

import com.demo.filmdb.person.dtos.PersonDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PersonMapper {

    PersonDto personToPersonDto(Person person);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updatePersonFromPersonInfo(PersonInfo personInfo, @MappingTarget Person person);
}
