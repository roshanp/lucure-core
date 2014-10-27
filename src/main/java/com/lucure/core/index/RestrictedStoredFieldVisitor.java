package com.lucure.core.index;

import com.lucure.core.security.ColumnVisibility;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 */
public abstract class RestrictedStoredFieldVisitor extends StoredFieldVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(RestrictedStoredFieldVisitor.class);

    public static final ColumnVisibility EMPTY = new ColumnVisibility();

    public RestrictedStoredFieldVisitor() {
    }

    @Override
    public void binaryField(FieldInfo fieldInfo, byte[] value) throws
                                                               IOException {
        binaryField(fieldInfo, value, EMPTY);
    }

    public abstract void binaryField(
      FieldInfo fieldInfo, byte[] value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void stringField(FieldInfo fieldInfo, String value) throws IOException {
        stringField(fieldInfo, value, EMPTY);
    }

    public abstract void stringField(
      FieldInfo fieldInfo, String value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void intField(FieldInfo fieldInfo, int value) throws IOException {
        intField(fieldInfo, value, EMPTY);
    }

    public abstract void intField(
      FieldInfo fieldInfo, int value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void longField(FieldInfo fieldInfo, long value) throws IOException {
        longField(fieldInfo, value, EMPTY);
    }

    public abstract void longField(
      FieldInfo fieldInfo, long value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void floatField(FieldInfo fieldInfo, float value)
      throws IOException {
        floatField(fieldInfo, value, EMPTY);
    }

    public abstract void floatField(
      FieldInfo fieldInfo, float value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void doubleField(FieldInfo fieldInfo, double value)
      throws IOException {
        doubleField(fieldInfo, value, EMPTY);
    }

    public abstract void doubleField(
      FieldInfo fieldInfo, double value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public Status needsField(FieldInfo fieldInfo) throws IOException {
        return needsField(fieldInfo, EMPTY);
    }

    public abstract Status needsField(
      FieldInfo fieldInfo, ColumnVisibility columnVisibility)
      throws IOException;
}
