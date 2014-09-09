package com.lucure.core;

import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;

import java.io.IOException;

/**
 */
public class DelegatingRestrictedFieldVisitor extends RestrictedStoredFieldVisitor {

    private final StoredFieldVisitor storedFieldVisitor;

    public DelegatingRestrictedFieldVisitor(
      StoredFieldVisitor storedFieldVisitor,
      VisibilityEvaluator visibilityEvaluator) {
        super(visibilityEvaluator);
        this.storedFieldVisitor = storedFieldVisitor;
    }

    @Override
    public void binaryField(
      FieldInfo fieldInfo, byte[] value, ColumnVisibility columnVisibility)
      throws IOException {
        storedFieldVisitor.binaryField(fieldInfo, value);
    }

    @Override
    public void stringField(
      FieldInfo fieldInfo, String value, ColumnVisibility columnVisibility)
      throws IOException {
        storedFieldVisitor.stringField(fieldInfo, value);
    }

    @Override
    public void intField(
      FieldInfo fieldInfo, int value, ColumnVisibility columnVisibility)
      throws IOException {
        storedFieldVisitor.intField(fieldInfo, value);
    }

    @Override
    public void longField(
      FieldInfo fieldInfo, long value, ColumnVisibility columnVisibility)
      throws IOException {
        storedFieldVisitor.longField(fieldInfo, value);
    }

    @Override
    public void floatField(
      FieldInfo fieldInfo, float value, ColumnVisibility columnVisibility)
      throws IOException {
        storedFieldVisitor.floatField(fieldInfo, value);
    }

    @Override
    public void doubleField(
      FieldInfo fieldInfo, double value, ColumnVisibility columnVisibility)
      throws IOException {
        storedFieldVisitor.doubleField(fieldInfo, value);
    }

    @Override
    public Status needsField(
      FieldInfo fieldInfo, ColumnVisibility columnVisibility)
      throws IOException {
        Status status = storedFieldVisitor.needsField(fieldInfo);
        if(status == Status.STOP || status == Status.NO) {
            return status;
        }
        return super.needsField(fieldInfo, columnVisibility);
    }
}
