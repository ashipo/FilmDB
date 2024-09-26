package com.demo.filmdb.film;

import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FilmInfoMapper {

    Film filmInfoToFilm(FilmInfo filmInfo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateFilmFromFilmInfo(FilmInfo filmInfo, @MappingTarget Film film);
}
