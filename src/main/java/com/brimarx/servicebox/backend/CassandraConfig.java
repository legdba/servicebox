package com.brimarx.servicebox.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;

public class CassandraConfig {
    public Collection<String> getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(Collection<String> contactPoints) {
        this.contactPoints = contactPoints;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Collection<String> contactPoints;
}
