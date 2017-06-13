// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.filedistribution.fileacquirer;

/**
 * @author tonytv
 */
public class FileReferenceDoesNotExistException extends RuntimeException {

    public final String fileReference;

    FileReferenceDoesNotExistException(String fileReference) {
        super("Could not retrieve file with file reference '" + fileReference + "'");
        this.fileReference = fileReference;
    }

}
