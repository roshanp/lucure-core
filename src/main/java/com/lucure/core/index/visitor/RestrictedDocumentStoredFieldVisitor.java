package com.lucure.core.index.visitor;

import com.google.common.collect.Sets;
import com.lucure.core.RestrictedField;
import com.lucure.core.security.FieldVisibility;
import org.apache.lucene.document.*;
import org.apache.lucene.index.FieldInfo;

import java.io.IOException;
import java.util.Set;

/**
 * Analog to {@link DocumentStoredFieldVisitor} that saves {@link com.lucure.core.RestrictedField}s
 */
public class RestrictedDocumentStoredFieldVisitor extends
                                                  RestrictedStoredFieldVisitor {


    private final Document doc = new Document();
    private final Set<String> fieldsToAdd;

    public RestrictedDocumentStoredFieldVisitor(
      Set<String> fieldsToAdd) {
        this.fieldsToAdd = fieldsToAdd;
    }

    public RestrictedDocumentStoredFieldVisitor(String... fields) {
        this(Sets.newHashSet(fields));
    }

    public RestrictedDocumentStoredFieldVisitor() {
        this((Set<String>) null);
    }

    @Override
    public void binaryField(FieldInfo fieldInfo, byte[] value, FieldVisibility fieldVisibility) throws
                                                               IOException {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), fieldVisibility));
    }

    @Override
    public void stringField(FieldInfo fieldInfo, String value, FieldVisibility fieldVisibility) throws IOException {
        final FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setStoreTermVectors(fieldInfo.hasVectors());
        ft.setIndexed(fieldInfo.isIndexed());
        ft.setOmitNorms(fieldInfo.omitsNorms());
        ft.setIndexOptions(fieldInfo.getIndexOptions());
        doc.add(new RestrictedField(new Field(fieldInfo.name, value, ft), fieldVisibility));
    }

    @Override
    public void intField(FieldInfo fieldInfo, int value, FieldVisibility fieldVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), fieldVisibility));
    }

    @Override
    public void longField(FieldInfo fieldInfo, long value, FieldVisibility fieldVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), fieldVisibility));
    }

    @Override
    public void floatField(FieldInfo fieldInfo, float value, FieldVisibility fieldVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), fieldVisibility));
    }

    @Override
    public void doubleField(FieldInfo fieldInfo, double value, FieldVisibility fieldVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), fieldVisibility));
    }

    @Override
    public Status needsField(FieldInfo fieldInfo, FieldVisibility fieldVisibility) throws IOException {
        return fieldsToAdd == null || fieldsToAdd.contains(fieldInfo.name) ? Status.YES : Status.NO;
    }

    /**
     * Retrieve the visited document.
     * @return Document populated with stored fields. Note that only
     *         the stored information in the field instances is valid,
     *         data such as boosts, indexing options, term vector options,
     *         etc is not set.
     */
    public Document getDocument() {
        return doc;
    }
}
