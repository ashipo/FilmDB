package com.demo.filmdb.graphql.inputs;

import com.demo.filmdb.role.CastMember;

public record CastMemberInput(Long personId, String character) implements CastMember {

    @Override
    public Long getPersonId() {
        return personId;
    }

    @Override
    public String getCharacter() {
        return character;
    }
}
