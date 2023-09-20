package com.demo.filmdb.film;

import com.demo.filmdb.annotations.ApiPrefixRestController;
import com.demo.filmdb.film.dtos.FilmDto;
import com.demo.filmdb.film.dtos.FilmDtoInput;
import com.demo.filmdb.film.specifications.FilmWithReleaseAfter;
import com.demo.filmdb.film.specifications.FilmWithReleaseBefore;
import com.demo.filmdb.film.specifications.FilmWithTitle;
import com.demo.filmdb.person.Person;
import com.demo.filmdb.person.PersonModelAssembler;
import com.demo.filmdb.person.PersonService;
import com.demo.filmdb.person.dtos.PersonDto;
import com.demo.filmdb.role.*;
import com.demo.filmdb.role.dtos.FilmRoleDto;
import com.demo.filmdb.role.dtos.FilmRoleDtoInput;
import com.demo.filmdb.role.dtos.RoleDto;
import com.demo.filmdb.role.dtos.RoleDtoInput;
import com.demo.filmdb.utils.SortUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.demo.filmdb.utils.SpringDocConfig.*;

@ApiPrefixRestController
public class FilmController {

    private final FilmService filmService;
    private final PersonService personService;
    private final RoleService roleService;
    private final FilmModelAssembler filmModelAssembler;
    private final PersonModelAssembler personModelAssembler;
    private final FilmRoleModelAssembler filmRoleModelAssembler;
    private final RoleModelAssembler roleModelAssembler;
    private final FilmMapper filmMapper;
    private final RoleMapper roleMapper;
    private final PagedResourcesAssembler<Film> pagedResourcesAssembler;

    public FilmController(FilmService filmService,
                          PersonService personService,
                          RoleService roleService,
                          FilmModelAssembler filmModelAssembler,
                          PersonModelAssembler personModelAssembler,
                          FilmRoleModelAssembler filmRoleModelAssembler,
                          RoleModelAssembler roleModelAssembler,
                          FilmMapper filmMapper, RoleMapper roleMapper,
                          PagedResourcesAssembler<Film> pagedResourcesAssembler) {
        this.filmService = filmService;
        this.personService = personService;
        this.roleService = roleService;
        this.filmModelAssembler = filmModelAssembler;
        this.personModelAssembler = personModelAssembler;
        this.filmRoleModelAssembler = filmRoleModelAssembler;
        this.roleModelAssembler = roleModelAssembler;
        this.filmMapper = filmMapper;
        this.roleMapper = roleMapper;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Operation(summary = "Find films", tags = TAG_FILMS)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successful operation") })
    @GetMapping(value = "/films/search", produces = "application/json")
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
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successful operation") })
    @GetMapping(value = "/films", produces = "application/json")
    public CollectionModel<FilmDto> getAllFilms(Pageable pageable) {
        Pageable filteredPageable = SortUtil.filterSort(pageable, Film.class);
        Page<Film> filmsPage = filmService.getAllFilms(filteredPageable);
        return pagedResourcesAssembler.toModel(filmsPage, filmModelAssembler);
    }

    @Operation(summary = "Create a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Film created"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content) })
    @PostMapping(value = "/films", produces = "application/json")
    public ResponseEntity<FilmDto> createFilm(@RequestBody @Valid FilmDtoInput filmDtoInput) {
        Film film = filmMapper.filmDtoInputToFilm(filmDtoInput);
        FilmDto newFilmDto = filmModelAssembler.toModel(filmService.saveFilm(film));
        return ResponseEntity.created(newFilmDto.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(newFilmDto);
    }

    @Operation(summary = "Get a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Film found"),
            @ApiResponse(responseCode = "404", description = "Film not found", content = @Content) })
    @GetMapping(value = "/films/{filmId}", produces = "application/json")
    public FilmDto getFilm(@PathVariable Long filmId) {
        Film film = filmService.getFilm(filmId);
        return filmModelAssembler.toModel(film);
    }

    @Operation(summary = "Update a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Film updated"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film not found", content = @Content) })
    @PutMapping(value = "/films/{filmId}", produces = "application/json")
    public FilmDto updateFilm(@PathVariable Long filmId, @Valid @RequestBody FilmDtoInput filmDtoInput) {
        Film filmToUpdate = filmService.getFilm(filmId);
        Film updatedFilm = filmMapper.updateFilmFromFilmDtoInput(filmDtoInput, filmToUpdate);
        Film savedFilm = filmService.saveFilm(updatedFilm);
        return filmModelAssembler.toModel(savedFilm);
    }

    @Operation(summary = "Delete a film", tags = TAG_FILMS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Film deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film not found", content = @Content) })
    @DeleteMapping("/films/{filmId}")
    public ResponseEntity<?> deleteFilm(@PathVariable Long filmId) {
        filmService.deleteFilm(filmId);
        return ResponseEntity.noContent().build();
    }

    /* Directors */

    @Operation(summary = "Get film directors", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Film not found", content = @Content) })
    @GetMapping(value = "/films/{filmId}/directors", produces = "application/json")
    public CollectionModel<PersonDto> getDirectors(@PathVariable Long filmId) {
        Collection<Person> directors = filmService.getDirectors(filmId);
        return personModelAssembler.directorsCollectionModel(directors, filmId);
    }

    @Operation(summary = "Update film directors", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Directors updated"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film or Person not found", content = @Content) })
    @PutMapping(value = "/films/{filmId}/directors", produces = "application/json")
    public CollectionModel<PersonDto> updateDirectors(@PathVariable Long filmId, @RequestBody List<Long> directorIds) {
        List<Person> directors = personService.getPeople(directorIds);
        Film updatedFilm = filmService.updateDirectors(filmId, directors);
        return personModelAssembler.directorsCollectionModel(updatedFilm.getDirectors(), filmId);
    }

    @Operation(summary = "Delete film directors", tags = TAG_DIRECTORS)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Directors deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film not found", content = @Content) })
    @DeleteMapping("/films/{filmId}/directors")
    public ResponseEntity<?> deleteDirectors(@PathVariable Long filmId) {
        filmService.deleteDirectors(filmId);
        return ResponseEntity.noContent().build();
    }

    /* Cast */

    @Operation(summary = "Get film cast", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Film not found", content = @Content) })
    @GetMapping(value = "/films/{filmId}/cast", produces = "application/json")
    public CollectionModel<FilmRoleDto> getCast(@PathVariable Long filmId) {
        Collection<Role> cast = filmService.getCast(filmId);
        return filmRoleModelAssembler.toCollectionModel(cast, filmId);
    }

    @Operation(summary = "Update film cast", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cast updated"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film or Person not found", content = @Content) })
    @PutMapping(value = "/films/{filmId}/cast", produces = "application/json")
    public CollectionModel<FilmRoleDto> updateCast(@PathVariable Long filmId, @RequestBody @Valid Set<FilmRoleDtoInput> newRoleDtos) {
        Film film = filmService.getFilm(filmId);
        Map<Person, String> newCast = newRoleDtos.stream().
                collect(Collectors.toMap(dto -> personService.getPerson(dto.personId()), FilmRoleDtoInput::character));
        Set<Role> updatedCast = roleService.updateCast(film, newCast);
        return filmRoleModelAssembler.toCollectionModel(updatedCast, filmId);
    }

    @Operation(summary = "Delete film cast", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cast deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film not found", content = @Content) })
    @DeleteMapping("/films/{filmId}/cast")
    public ResponseEntity<?> deleteCast(@PathVariable Long filmId) {
        filmService.deleteCast(filmId);
        return ResponseEntity.noContent().build();
    }

    /* Role */

    @Operation(summary = "Create a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Film or Person not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Role already exists", content = @Content) })
    @PostMapping(value = "/films/{filmId}/cast", produces = "application/json")
    public ResponseEntity<RoleDto> createRole(@PathVariable Long filmId, @RequestBody @Valid FilmRoleDtoInput roleDto) {
        Film film = filmService.getFilm(filmId);
        Person person = personService.getPerson(roleDto.personId());
        Role newRole = roleService.createRole(film, person, roleDto.character());
        RoleDto newRoleDto = roleModelAssembler.toModel(newRole);
        return ResponseEntity.created(newRoleDto.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(newRoleDto);
    }

    @Operation(summary = "Get a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content) })
    @GetMapping(value = "/films/{filmId}/cast/{personId}", produces = "application/json")
    public RoleDto getRole(@PathVariable Long filmId, @PathVariable Long personId) {
        Role role = roleService.getRole(filmId, personId);
        return roleModelAssembler.toModel(role);
    }

    @Operation(summary = "Update a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated"),
            @ApiResponse(responseCode = "400", description = "Malformed request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content) })
    @PatchMapping(value = "/films/{filmId}/cast/{personId}", produces = "application/json")
    public RoleDto updateRole(@PathVariable Long filmId, @PathVariable Long personId,
                                        @RequestBody @Valid RoleDtoInput inputDto) {
        Role role = roleService.getRole(filmId, personId);
        Role updatedRole = roleMapper.updateRoleFromRoleDtoInput(inputDto, role);
        Role savedRole = roleService.saveRole(updatedRole);
        return roleModelAssembler.toModel(savedRole);
    }

    @Operation(summary = "Delete a role", tags = TAG_ROLES)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content) })
    @DeleteMapping("/films/{filmId}/cast/{personId}")
    public ResponseEntity<?> deleteRole(@PathVariable Long filmId, @PathVariable Long personId) {
        roleService.deleteRole(filmId, personId);
        return ResponseEntity.noContent().build();
    }
}
