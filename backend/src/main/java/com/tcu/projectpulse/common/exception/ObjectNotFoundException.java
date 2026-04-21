package com.tcu.projectpulse.common.exception;

public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(String objectName, Object id) {
        super(objectName + " not found with id: " + id);
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }
}
