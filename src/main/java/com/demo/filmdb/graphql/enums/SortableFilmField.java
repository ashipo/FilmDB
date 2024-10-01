package com.demo.filmdb.graphql.enums;

/**
 * Represents sortable fields of a film. Must match GraphQL enum with the same name.
 */
public enum SortableFilmField {
    ID("id"),
    TITLE("title"),
    RELEASE_DATE("releaseDate");

    private final String fieldName;

    SortableFilmField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Returns corresponding entity field name
     *
     * @return field name
     */
    public String getFieldName() {
        return fieldName;
    }
}
