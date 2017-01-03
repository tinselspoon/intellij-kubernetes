package com.github.tinselspoon.intellij.kubernetes.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines an API, which is a set of several operations on a particular object.
 */
class Api {

    /** Descriptions of the operations possible on this API. */
    private final List<ApiOperation> operations = new ArrayList<>();

    /**
     * Gets descriptions of the operations possible on this API.
     *
     * @return the operations.
     */
    public List<ApiOperation> getOperations() {
        return operations;
    }
}
