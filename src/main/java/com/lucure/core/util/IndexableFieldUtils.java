package com.lucure.core.util;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;

/**
 * Various static utilities to work with Lucene {@link IndexableField}s
 */
public class IndexableFieldUtils {

    public static Object toObject(IndexableField field) {
        Object data = field.readerValue();
        if(data == null) {
            data = field.numericValue();
            if (data == null) {
                data = field.binaryValue();
                if (data == null) {
                    data = field.stringValue();
                }
            }
        }
        return data;
    }

    public static FieldType from(IndexableFieldType indexableFieldType) {
        final FieldType fieldType = new FieldType();
        fieldType.setStored(indexableFieldType.stored());
        fieldType.setIndexed(indexableFieldType.indexed());
        fieldType.setIndexOptions(indexableFieldType.indexOptions());
        fieldType.setStoreTermVectors(indexableFieldType.storeTermVectors());
        fieldType.setStoreTermVectorPositions(indexableFieldType.storeTermVectorPositions());
        fieldType.setStoreTermVectorPayloads(indexableFieldType.storeTermVectorPayloads());
        fieldType.setStoreTermVectorOffsets(indexableFieldType.storeTermVectorOffsets());
        fieldType.setDocValueType(indexableFieldType.docValueType());
        fieldType.setOmitNorms(indexableFieldType.omitNorms());
        fieldType.setTokenized(indexableFieldType.tokenized());
        return fieldType;
    }

}
