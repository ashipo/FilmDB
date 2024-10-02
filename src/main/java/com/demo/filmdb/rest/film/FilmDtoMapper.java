package com.demo.filmdb.rest.film;

import com.demo.filmdb.film.Film;
import com.demo.filmdb.rest.film.dtos.FilmDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FilmDtoMapper {

    FilmDto filmToFilmDto(Film film);
}
