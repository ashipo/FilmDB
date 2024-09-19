package com.demo.filmdb.film;

import com.demo.filmdb.film.dtos.FilmDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FilmMapper {

    FilmDto filmToFilmDto(Film film);

    Film filmInfoToFilm(FilmInfo filmInfo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateFilmFromFilmInfo(FilmInfo filmInfo, @MappingTarget Film film);
}
