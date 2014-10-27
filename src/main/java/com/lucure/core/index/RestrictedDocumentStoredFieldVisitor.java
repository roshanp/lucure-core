package com.lucure.core.index;

import com.google.common.collect.Sets;
import com.lucure.core.RestrictedField;
import com.lucure.core.security.ColumnVisibility;
import com.lucure.core.security.VisibilityEvaluator;
import org.apache.lucene.document.*;
import org.apache.lucene.index.FieldInfo;

import java.io.IOException;
import java.util.Set;

/**
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
    public void binaryField(FieldInfo fieldInfo, byte[] value, ColumnVisibility columnVisibility) throws
                                                               IOException {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), columnVisibility));
    }

    @Override
    public void stringField(FieldInfo fieldInfo, String value, ColumnVisibility columnVisibility) throws IOException {
        final FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setStoreTermVectors(fieldInfo.hasVectors());
        ft.setIndexed(fieldInfo.isIndexed());
        ft.setOmitNorms(fieldInfo.omitsNorms());
        ft.setIndexOptions(fieldInfo.getIndexOptions());
        doc.add(new RestrictedField(new Field(fieldInfo.name, value, ft), columnVisibility));
    }

    @Override
    public void intField(FieldInfo fieldInfo, int value, ColumnVisibility columnVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), columnVisibility));
    }

    @Override
    public void longField(FieldInfo fieldInfo, long value, ColumnVisibility columnVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), columnVisibility));
    }

    @Override
    public void floatField(FieldInfo fieldInfo, float value, ColumnVisibility columnVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), columnVisibility));
    }

    @Override
    public void doubleField(FieldInfo fieldInfo, double value, ColumnVisibility columnVisibility) {
        doc.add(new RestrictedField(new StoredField(fieldInfo.name, value), columnVisibility));
    }

    @Override
    public Status needsField(FieldInfo fieldInfo, ColumnVisibility columnVisibility) throws IOException {
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
