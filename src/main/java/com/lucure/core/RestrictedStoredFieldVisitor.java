package com.lucure.core;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
import org.apache.accumulo.core.security.VisibilityParseException;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.index.FieldInfo;

import java.io.IOException;

/**
 * Restrict stored fields based on Column Visibility embedded in string value
 */
public final class RestrictedStoredFieldVisitor
  extends DocumentStoredFieldVisitor {

    private final VisibilityEvaluator visibilityEvaluator;

    public RestrictedStoredFieldVisitor(String... fields) {
        this(Authorizations.EMPTY, fields);
    }

    public RestrictedStoredFieldVisitor(
      Authorizations authorizations, String... fields) {
        super(fields);
        this.visibilityEvaluator = new VisibilityEvaluator(authorizations);
    }

    @Override
    public Status needsField(FieldInfo fieldInfo) throws IOException {
        return Status.YES;
    }

    public boolean hasAccess(ColumnVisibility cv)
      throws VisibilityParseException {
        return visibilityEvaluator.evaluate(cv);
    }
}