package com.demo.filmdb.person;

import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface PersonInfoMapper {

    Person personInfoToPerson(PersonInfo personInfo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updatePersonFromPersonInfo(PersonInfo personInfo, @MappingTarget Person person);
}
