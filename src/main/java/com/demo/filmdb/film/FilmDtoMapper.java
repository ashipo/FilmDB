package com.demo.filmdb.film;

import com.demo.filmdb.film.dtos.FilmDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FilmDtoMapper {

    FilmDto filmToFilmDto(Film film);
}
