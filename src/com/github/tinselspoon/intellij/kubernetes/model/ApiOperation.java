package com.github.tinselspoon.intellij.kubernetes.model;

/**
 * Defines an operation that may be invoked on an API.
 */
class ApiOperation {

    /** The HTTP method required to invoke this API. */
    private String method;

    /** The type of data this API is based on. */
    private String type;

    /**
     * Gets the HTTP method required to invoke this API.
     *
     * @return the method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Gets the type of data this API is based on.
     *
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the HTTP method required to invoke this API.
     *
     * @param method the new method.
     */
    public void setMethod(final String method) {
        this.method = method;
    }

    /**
     * Sets the type of data this API is based on.
     *
     * @param type the new type.
     */
    public void setType(final String type) {
        this.type = type;
    }
}
