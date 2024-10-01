package com.demo.filmdb.graphql.enums;

/**
 * Represents sortable fields of a person. Must match GraphQL enum with the same name.
 */
public enum SortablePersonField {
    ID("id"),
    NAME("name"),
    DATE_OF_BIRTH("dateOfBirth");

    private final String fieldName;

    SortablePersonField(String fieldName) {
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
