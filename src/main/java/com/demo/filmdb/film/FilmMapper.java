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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateFilmFromFilmInfo(FilmInfo filmInfo, @MappingTarget Film film);
}
