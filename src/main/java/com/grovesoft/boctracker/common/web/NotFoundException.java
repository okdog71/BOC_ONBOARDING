package com.grovesoft.boctracker.common.web;

public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
