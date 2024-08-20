package com.demo.filmdb.film;

import com.demo.filmdb.director.DirectorService;
import com.demo.filmdb.film.dtos.FilmDto;
import com.demo.filmdb.film.dtos.FilmDtoInput;
import com.demo.filmdb.film.specifications.FilmWithReleaseAfter;
import com.demo.filmdb.film.specifications.FilmWithReleaseBefore;
import com.demo.filmdb.film.specifications.FilmWithTitle;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonModelAssembler;
import com.demo.filmdb.person.dtos.PersonDto;
import com.demo.filmdb.role.FilmRoleModelAssembler;
import com.demo.filmdb.role.Role;
import com.demo.filmdb.role.RoleModelAssembler;
import com.demo.filmdb.role.RoleService;
import com.demo.filmdb.role.dtos.FilmRoleDto;
import com.demo.filmdb.role.dtos.FilmRoleDtoInput;
import com.demo.filmdb.role.dtos.RoleDto;
import com.demo.filmdb.role.dtos.RoleDtoInput;
import com.demo.filmdb.util.EntityAlreadyExistsException;
import com.demo.filmdb.util.EntityNotFoundException;
import com.demo.filmdb.utils.SortUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static com.demo.filmdb.util.ErrorUtil.filmNotFoundMessage;
import static com.demo.filmdb.util.ErrorUtil.roleNotFoundMessage;
import static com.demo.filmdb.utils.Path.API_PREFIX;
import static com.demo.filmdb.utils.Path.FILM;
import static com.demo.filmdb.utils.SpringDocConfig.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = API_PREFIX + FILM, produces = APPLICATION_JSON_VALUE)
public class FilmController {

    private final FilmService filmService;
    private final RoleService roleService;
    private final DirectorService directorService;
    private final FilmModelAssembler filmModelAssembler;
    private final PersonModelAssembler personModelAssembler;
    private final FilmRoleModelAssembler filmRoleModelAssembler;
    private final RoleModelAssembler roleModelAssembler;
    private final PagedResourcesAssembler<Film> pagedResourcesAssembler;

    public FilmController(FilmService filmService,
                          RoleService roleService,
                          DirectorService directorService,
                          FilmModelAssembler filmModelAssembler,
                          PersonModelAssembler personModelAssembler,
                          FilmRoleModelAssembler filmRoleModelAssembler,
                          RoleModelAssembler roleModelAssembler,
                          PagedResourcesAssembler<Film> pagedResourcesAssembler) {
        this.filmService = filmService;
        this.roleService = roleService;
        this.directorService = directorService;
        this.filmModelAssembler = filmModelAssembler;
        this.personModelAssembler = personModelAssembler;
        this.filmRoleModelAssembler = filmRoleModelAssembler;
        this.roleModelAssembler = roleModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Find films", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
    })
    @SecurityRequirements
    @GetMapping("/search")
    public CollectionModel<FilmDto> findFilms(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "release_after", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseAfter,
            @RequestParam(value = "release_before", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseBefore,
            Pageable pageable) {
        Specification<Film> spec = Specification.where(new FilmWithTitle(title)).
                and(new FilmWithReleaseBefore(releaseBefore)).
                and(new FilmWithReleaseAfter(releaseAfter));
        Pageable filteredPageable = SortUtil.filterSort(pageable, Film.class);
        Page<Film> filmsFound = filmService.search(spec, filteredPageable);
        return pagedResourcesAssembler.toModel(filmsFound, filmModelAssembler);
    }

    @Operation(summary = "List all films", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
    })
    @SecurityRequirements
    @GetMapping
    public CollectionModel<FilmDto> getAllFilms(Pageable pageable) {
        Pageable filteredPageable = SortUtil.filterSort(pageable, Film.class);
        Page<Film> filmsPage = filmService.getAllFilms(filteredPageable);
        return pagedResourcesAssembler.toModel(filmsPage, filmModelAssembler);
    }

    @Operation(summary = "Create a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Film created"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "create a film", content = @Content),
    })
    @PostMapping
    public ResponseEntity<FilmDto> createFilm(@RequestBody @Valid FilmDtoInput filmDtoInput) {
        Film createdFilm = filmService.createFilm(filmDtoInput);
        FilmDto createdFilmDto = filmModelAssembler.toModel(createdFilm);
        return ResponseEntity.created(createdFilmDto.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(createdFilmDto);
    }

    @Operation(summary = "Get a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Film found"),
            @ApiResponse(responseCode = "404", description = FILM_NOT_FOUND, content = @Content),
    })
    @SecurityRequirements
    @GetMapping("/{filmId}")
    public FilmDto getFilm(@PathVariable Long filmId) {
        Film film = filmService.getFilm(filmId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, filmNotFoundMessage(filmId))
        );
        return filmModelAssembler.toModel(film);
    }

    @Operation(summary = "Update a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Film updated"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "update a film", content = @Content),
            @ApiResponse(responseCode = "404", description = FILM_NOT_FOUND, content = @Content),
    })
    @PutMapping("/{filmId}")
    public FilmDto updateFilm(@PathVariable Long filmId, @Valid @RequestBody FilmDtoInput filmDtoInput) {
        try {
            Film updatedFilm = filmService.updateFilm(filmId, filmDtoInput);
            return filmModelAssembler.toModel(updatedFilm);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Delete a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Film deleted", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "delete a film", content = @Content),
            @ApiResponse(responseCode = "404", description = FILM_NOT_FOUND, content = @Content),
    })
    @DeleteMapping("/{filmId}")
    public ResponseEntity<?> deleteFilm(@PathVariable Long filmId) {
        if (!filmService.filmExists(filmId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, filmNotFoundMessage(filmId));
        }
        filmService.deleteFilm(filmId);
        return ResponseEntity.noContent().build();
    }

    /* Directors */

    @Operation(summary = "Get film directors", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
            @ApiResponse(responseCode = "404", description = FILM_NOT_FOUND, content = @Content),
    })
    @SecurityRequirements
    @GetMapping("/{filmId}/directors")
    public CollectionModel<PersonDto> getDirectors(@PathVariable Long filmId) {
        Film film = filmService.getFilm(filmId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, filmNotFoundMessage(filmId))
        );
        Collection<Person> directors = film.getDirectors();
        return personModelAssembler.directorsCollectionModel(directors, filmId);
    }

    @Operation(summary = "Update film directors", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Directors updated"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "update film directors", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film or Person not found", content = @Content),
    })
    @PutMapping("/{filmId}/directors")
    public CollectionModel<PersonDto> updateDirectors(@PathVariable Long filmId, @RequestBody List<Long> directorsIds) {
        try {
            List<Person> directors = directorService.updateDirectors(filmId, directorsIds);
            return personModelAssembler.directorsCollectionModel(directors, filmId);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Delete film directors", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Directors deleted", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "delete film directors", content = @Content),
            @ApiResponse(responseCode = "404", description = FILM_NOT_FOUND, content = @Content),
    })
    @DeleteMapping("/{filmId}/directors")
    public ResponseEntity<?> deleteDirectors(@PathVariable Long filmId) {
        try {
            directorService.deleteDirectors(filmId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /* Cast */

    @Operation(summary = "Get film cast", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
            @ApiResponse(responseCode = "404", description = FILM_NOT_FOUND, content = @Content),
    })
    @SecurityRequirements
    @GetMapping("/{filmId}/cast")
    public CollectionModel<FilmRoleDto> getCast(@PathVariable Long filmId) {
        Film film = filmService.getFilm(filmId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, filmNotFoundMessage(filmId))
        );
        Collection<Role> cast = film.getCast();
        return filmRoleModelAssembler.toCollectionModel(cast, film.getId());
    }

    @Operation(summary = "Update film cast", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cast updated"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "update film cast", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film or Person not found", content = @Content),
    })
    @PutMapping("/{filmId}/cast")
    public CollectionModel<FilmRoleDto> updateCast(@PathVariable Long filmId, @RequestBody @Valid List<FilmRoleDtoInput> newRoleDtos) {
        try {
            List<Role> updatedCast = roleService.updateCast(filmId, newRoleDtos);
            return filmRoleModelAssembler.toCollectionModel(updatedCast, filmId);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }

    @Operation(summary = "Delete film cast", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cast deleted", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "delete film cast", content = @Content),
            @ApiResponse(responseCode = "404", description = FILM_NOT_FOUND, content = @Content),
    })
    @DeleteMapping("/{filmId}/cast")
    public ResponseEntity<?> deleteCast(@PathVariable Long filmId) {
        if (!filmService.filmExists(filmId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, filmNotFoundMessage(filmId));
        }
        roleService.deleteCast(filmId);
        return ResponseEntity.noContent().build();
    }

    /* Role */

    @Operation(summary = "Create a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "create a role", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film or Person not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Role already exists", content = @Content),
    })
    @PostMapping("/{filmId}/cast")
    public ResponseEntity<RoleDto> createRole(@PathVariable Long filmId, @RequestBody @Valid FilmRoleDtoInput roleDto) {
        try {
            Role createdRole = roleService.createRole(filmId, roleDto.personId(), roleDto.character());
            RoleDto createdRoleDto = roleModelAssembler.toModel(createdRole);
            return ResponseEntity
                    .created(createdRoleDto.getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(createdRoleDto);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (EntityAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Operation(summary = "Get a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = SUCCESS),
            @ApiResponse(responseCode = "404", description = ROLE_NOT_FOUND, content = @Content),
    })
    @SecurityRequirements
    @GetMapping("/{filmId}/cast/{personId}")
    public RoleDto getRole(@PathVariable Long filmId, @PathVariable Long personId) {
        Role role = roleService.getRole(filmId, personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, roleNotFoundMessage(filmId, personId))
        );
        return roleModelAssembler.toModel(role);
    }

    @Operation(summary = "Update a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated"),
            @ApiResponse(responseCode = "400", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "update a role", content = @Content),
            @ApiResponse(responseCode = "404", description = ROLE_NOT_FOUND, content = @Content),
    })
    @PatchMapping("/{filmId}/cast/{personId}")
    public RoleDto updateRole(@PathVariable Long filmId, @PathVariable Long personId, @RequestBody @Valid RoleDtoInput inputDto) {
        try {
            Role updatedRole = roleService.updateRole(filmId, personId, inputDto.character());
            return roleModelAssembler.toModel(updatedRole);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Delete a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted", content = @Content),
            @ApiResponse(responseCode = "403", description = UNAUTHORIZED_TO + "delete a role", content = @Content),
            @ApiResponse(responseCode = "404", description = ROLE_NOT_FOUND, content = @Content),
    })
    @DeleteMapping("/{filmId}/cast/{personId}")
    public ResponseEntity<?> deleteRole(@PathVariable Long filmId, @PathVariable Long personId) {
        if (!roleService.roleExists(filmId, personId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, roleNotFoundMessage(filmId, personId));
        }
        roleService.deleteRole(filmId, personId);
        return ResponseEntity.noContent().build();
    }
}
