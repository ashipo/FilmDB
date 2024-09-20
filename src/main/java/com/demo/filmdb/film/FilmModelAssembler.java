package com.demo.filmdb.film;

import com.demo.filmdb.film.dtos.FilmDto;
import com.demo.filmdb.person.PersonController;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class FilmModelAssembler implements RepresentationModelAssembler<Film, FilmDto> {

    private final FilmDtoMapper filmMapper;

    public FilmModelAssembler(FilmDtoMapper filmMapper) {
        this.filmMapper = filmMapper;
    }

    @Override
    public FilmDto toModel(Film film) {
        FilmDto filmDto = filmMapper.filmToFilmDto(film);
        filmDto.add(
                linkTo(methodOn(FilmController.class).getFilm(film.getId())).withSelfRel(),
                linkTo(methodOn(FilmController.class).getDirectors(film.getId())).withRel("directors"),
                linkTo(methodOn(FilmController.class).getCast(film.getId())).withRel("cast"),
                linkTo(methodOn(FilmController.class).getAllFilms(Pageable.unpaged())).withRel("films")
        );
        return filmDto;
    }

    @Override
    public CollectionModel<FilmDto> toCollectionModel(Iterable<? extends Film> entities) {
        CollectionModel<FilmDto> result = RepresentationModelAssembler.super.toCollectionModel(entities);
        result.add(linkTo(methodOn(FilmController.class).getAllFilms(Pageable.unpaged())).withSelfRel());
        return result;
    }

    public CollectionModel<FilmDto> directedFilmsCollectionModel(Iterable<? extends Film> films, Long directorId) {
        CollectionModel<FilmDto> result = RepresentationModelAssembler.super.toCollectionModel(films);
        result.add(linkTo(methodOn(PersonController.class).getFilmsDirected(directorId)).withSelfRel());
        result.add(linkTo(methodOn(PersonController.class).getPerson(directorId)).withRel("person"));
        return result;
    }
}
