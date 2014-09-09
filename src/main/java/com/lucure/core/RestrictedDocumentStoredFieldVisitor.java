package com.lucure.core;

import com.google.common.collect.Sets;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
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
      Set<String> fieldsToAdd, VisibilityEvaluator visibilityEvaluator) {
        super(visibilityEvaluator);
        this.fieldsToAdd = fieldsToAdd;
    }

    public RestrictedDocumentStoredFieldVisitor(VisibilityEvaluator visibilityEvaluator, String... fields) {
        this(Sets.newHashSet(fields), visibilityEvaluator);
    }

    public RestrictedDocumentStoredFieldVisitor(VisibilityEvaluator visibilityEvaluator) {
        this(null, visibilityEvaluator);
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
        boolean addField = fieldsToAdd == null || fieldsToAdd.contains(fieldInfo.name);
        if(!addField) {
            return Status.NO;
        }
        return super.needsField(fieldInfo, columnVisibility);
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
