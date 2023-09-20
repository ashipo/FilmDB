package com.demo.filmdb.film;

import com.demo.filmdb.film.dtos.FilmDto;
import com.demo.filmdb.film.dtos.FilmDtoInput;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FilmMapper {

    /* FilmDto */

    FilmDto filmToFilmDto(Film film);

    /* FilmDtoInput */

    Film filmDtoInputToFilm(FilmDtoInput filmDtoInput);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Film updateFilmFromFilmDtoInput(FilmDtoInput filmDtoInput, @MappingTarget Film film);
}
