package com.bah.lucure.core;

import java.io.IOException;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
import org.apache.accumulo.core.security.VisibilityParseException;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.FieldInfo;

/**
 * Restrict stored fields based on Column Visibility embedded in string value
 */
public final class RestrictedStoredFieldVisitor extends
    DocumentStoredFieldVisitor {

  private final VisibilityEvaluator visibilityEvaluator;

  public RestrictedStoredFieldVisitor(Authorizations authorizations,
                                      String... fields) {
    super(fields);
    this.visibilityEvaluator = new VisibilityEvaluator(authorizations);
  }

  @Override
  public void stringField(FieldInfo fieldInfo, String value)
      throws IOException {
    final FieldType ft = new FieldType(TextField.TYPE_STORED);
    ft.setStoreTermVectors(fieldInfo.hasVectors());
    ft.setIndexed(fieldInfo.isIndexed());
    ft.setOmitNorms(fieldInfo.omitsNorms());
    ft.setIndexOptions(fieldInfo.getIndexOptions());

    int i = value.lastIndexOf(RestrictedField.DELIM);
    if (i >= 0) {
      //restricted
      String cv = value.substring(i + 1);
      ColumnVisibility columnVisibility = new ColumnVisibility(cv);
      try {
        if (!visibilityEvaluator.evaluate(columnVisibility)) {
          return;
        }
        value = value.substring(0, i);
        getDocument().add(
            new RestrictedField(fieldInfo.name, value, ft, columnVisibility));
      } catch (VisibilityParseException e) {
        throw new IOException(e);
      }
    } else {
      getDocument().add(new Field(fieldInfo.name, value, ft));
    }
  }

  @Override
  public Status needsField(FieldInfo fieldInfo) throws IOException {
    return Status.YES;
  }
}