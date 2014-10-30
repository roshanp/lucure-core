package com.lucure.core.index.visitor;

import com.lucure.core.security.FieldVisibility;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;

import java.io.IOException;

/**
 */
public class DelegatingRestrictedFieldVisitor extends RestrictedStoredFieldVisitor {

    /**
     * Only wrap with a {@link DelegatingRestrictedFieldVisitor} if necessary
     *
     * @param visitor To wrap
     * @return {@link RestrictedStoredFieldVisitor}
     */
    public static RestrictedStoredFieldVisitor wrap(
      StoredFieldVisitor visitor) {
        return visitor instanceof RestrictedStoredFieldVisitor ?
               (RestrictedStoredFieldVisitor) visitor :
               new DelegatingRestrictedFieldVisitor(visitor);
    }

    private final StoredFieldVisitor storedFieldVisitor;

    public DelegatingRestrictedFieldVisitor(
      StoredFieldVisitor storedFieldVisitor) {
        this.storedFieldVisitor = storedFieldVisitor;
    }

    @Override
    public void binaryField(
      FieldInfo fieldInfo, byte[] value, FieldVisibility fieldVisibility)
      throws IOException {
        storedFieldVisitor.binaryField(fieldInfo, value);
    }

    @Override
    public void stringField(
      FieldInfo fieldInfo, String value, FieldVisibility fieldVisibility)
      throws IOException {
        storedFieldVisitor.stringField(fieldInfo, value);
    }

    @Override
    public void intField(
      FieldInfo fieldInfo, int value, FieldVisibility fieldVisibility)
      throws IOException {
        storedFieldVisitor.intField(fieldInfo, value);
    }

    @Override
    public void longField(
      FieldInfo fieldInfo, long value, FieldVisibility fieldVisibility)
      throws IOException {
        storedFieldVisitor.longField(fieldInfo, value);
    }

    @Override
    public void floatField(
      FieldInfo fieldInfo, float value, FieldVisibility fieldVisibility)
      throws IOException {
        storedFieldVisitor.floatField(fieldInfo, value);
    }

    @Override
    public void doubleField(
      FieldInfo fieldInfo, double value, FieldVisibility fieldVisibility)
      throws IOException {
        storedFieldVisitor.doubleField(fieldInfo, value);
    }

    @Override
    public Status needsField(
      FieldInfo fieldInfo, FieldVisibility fieldVisibility)
      throws IOException {
        Status status = storedFieldVisitor.needsField(fieldInfo);
        if(status == Status.STOP || status == Status.NO) {
            return status;
        }
        return Status.YES;
    }
}
