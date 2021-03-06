// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.documentapi;

import com.yahoo.document.Document;

/**
 * The asynchronous response to a document put or get operation.
 * This is a <i>value object</i>.
 *
 * @author Einar M R Rosenvinge
 */
public class DocumentResponse extends Response {

    /** The document of this response, if any */
    private final Document document;

    /** Creates a successful response */
    public DocumentResponse(long requestId) {
        this(requestId, null);
    }

    /**
     * Creates a successful response containing a document
     *
     * @param document the Document to encapsulate in the Response
     */
    public DocumentResponse(long requestId, Document document) {
        super(requestId);
        this.document = document;
    }

    /**
     * Creates a response containing a textual message
     *
     * @param textMessage the message to encapsulate in the Response
     * @param success     true if the response represents a successful call
     */
    @Deprecated(since = "7") // TODO: Remove on Vespa 8
    public DocumentResponse(long requestId, String textMessage, boolean success) {
        super(requestId, textMessage, success);
        document = null;
    }

    /**
     * Creates a response containing a textual message
     *
     * @param textMessage the message to encapsulate in the Response
     * @param outcome     the outcome of this operation
     */
    public DocumentResponse(long requestId, String textMessage, Outcome outcome) {
        this(requestId, null, textMessage, outcome);
    }

    /**
     * Creates a response containing a textual message and/or a document
     *
     * @param document    the Document to encapsulate in the Response
     * @param textMessage the message to encapsulate in the Response
     * @param success     true if the response represents a successful call
     */
    @Deprecated(since = "7") // TODO: Remove on Vespa 8
    public DocumentResponse(long requestId, Document document, String textMessage, boolean success) {
        this(requestId, document, textMessage, success ? Outcome.SUCCESS : Outcome.ERROR);
    }


    /**
     * Creates a response containing a textual message and/or a document
     *
     * @param document    the Document to encapsulate in the Response
     * @param textMessage the message to encapsulate in the Response
     * @param outcome     the outcome of this operation
     */
    public DocumentResponse(long requestId, Document document, String textMessage, Outcome outcome) {
        super(requestId, textMessage, outcome);
        this.document = document;
    }


    /**
     * Returns the document of this response, or null if there is none
     *
     * @return the Document, or null
     */
    public Document getDocument() { return document; }

    public int hashCode() {
        return super.hashCode() + (document == null ? 0 : document.hashCode());
    }

    public boolean equals(Object o) {
        if (!(o instanceof DocumentResponse)) {
            return false;
        }

        DocumentResponse docResp = (DocumentResponse) o;

        return super.equals(docResp) && ((document == null && docResp.document == null) ||
                (document != null && docResp.document != null && document.equals(docResp.document)));
    }

    public String toString() {
        return "Document" + super.toString() + (document == null ? "" : " " + document);
    }

}
