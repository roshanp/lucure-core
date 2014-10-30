package com.lucure.core.index;

import com.lucure.core.security.FieldVisibility;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 */
public abstract class RestrictedStoredFieldVisitor extends StoredFieldVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(RestrictedStoredFieldVisitor.class);

    public static final FieldVisibility EMPTY = new FieldVisibility();

    public RestrictedStoredFieldVisitor() {
    }

    @Override
    public void binaryField(FieldInfo fieldInfo, byte[] value) throws
                                                               IOException {
        binaryField(fieldInfo, value, EMPTY);
    }

    public abstract void binaryField(
      FieldInfo fieldInfo, byte[] value, FieldVisibility fieldVisibility)
      throws IOException;

    @Override
    public void stringField(FieldInfo fieldInfo, String value) throws IOException {
        stringField(fieldInfo, value, EMPTY);
    }

    public abstract void stringField(
      FieldInfo fieldInfo, String value, FieldVisibility fieldVisibility)
      throws IOException;

    @Override
    public void intField(FieldInfo fieldInfo, int value) throws IOException {
        intField(fieldInfo, value, EMPTY);
    }

    public abstract void intField(
      FieldInfo fieldInfo, int value, FieldVisibility fieldVisibility)
      throws IOException;

    @Override
    public void longField(FieldInfo fieldInfo, long value) throws IOException {
        longField(fieldInfo, value, EMPTY);
    }

    public abstract void longField(
      FieldInfo fieldInfo, long value, FieldVisibility fieldVisibility)
      throws IOException;

    @Override
    public void floatField(FieldInfo fieldInfo, float value)
      throws IOException {
        floatField(fieldInfo, value, EMPTY);
    }

    public abstract void floatField(
      FieldInfo fieldInfo, float value, FieldVisibility fieldVisibility)
      throws IOException;

    @Override
    public void doubleField(FieldInfo fieldInfo, double value)
      throws IOException {
        doubleField(fieldInfo, value, EMPTY);
    }

    public abstract void doubleField(
      FieldInfo fieldInfo, double value, FieldVisibility fieldVisibility)
      throws IOException;

    @Override
    public Status needsField(FieldInfo fieldInfo) throws IOException {
        return needsField(fieldInfo, EMPTY);
    }

    public abstract Status needsField(
      FieldInfo fieldInfo, FieldVisibility fieldVisibility)
      throws IOException;
}
