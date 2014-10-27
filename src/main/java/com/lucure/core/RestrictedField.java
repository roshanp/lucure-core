package com.lucure.core;

import com.lucure.core.security.ColumnVisibility;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class RestrictedField extends Field {

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

  public final class ColumnVisibilityPayloadFilter extends TokenFilter {
    private final PayloadAttribute payAtt =
        addAttribute(PayloadAttribute.class);
    private final ColumnVisibility columnVisibility;


    public ColumnVisibilityPayloadFilter(TokenStream input,
                                         ColumnVisibility columnVisibility) {
      super(input);
      this.columnVisibility = columnVisibility;
    }

    @Override
    public boolean incrementToken() throws IOException {
      if (input.incrementToken()) {
        payAtt.setPayload(new BytesRef(columnVisibility.getExpression()));
        return true;
      } else {
        return false;
      }
    }
  }

  private final ColumnVisibility columnVisibility;

  public RestrictedField(String name, Object value, FieldType type,
                         ColumnVisibility columnVisibility) {
    super(name, type);
    this.fieldsData = value;
    this.columnVisibility = columnVisibility;
  }

  public RestrictedField(Field field, ColumnVisibility columnVisibility) {
    super(field.name(), field.fieldType());
      this.fieldsData = toObject(field);
      this.columnVisibility = columnVisibility;
  }

    public RestrictedField(IndexableField field, ColumnVisibility columnVisibility) {
        super(field.name(), from(field.fieldType()));
        this.fieldsData = toObject(field);
        this.columnVisibility = columnVisibility;
    }

    public ColumnVisibility getColumnVisibility() {
    return columnVisibility;
  }

  @Override
  public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) throws IOException {
    TokenStream tokenStream = super.tokenStream(analyzer, reuse);
    tokenStream = new ColumnVisibilityPayloadFilter(tokenStream, columnVisibility);
    return tokenStream;
  }

  @Override
  public String toString() {
    return "RestrictedField{" +
        "super=" + super.toString() +
        ", columnVisibility=" + columnVisibility +
        '}';
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RestrictedField)) {
            return false;
        }

        RestrictedField that = (RestrictedField) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (fieldsData != null ? !fieldsData.equals(that.fieldsData) : that.fieldsData != null) {
            return false;
        }
        if (columnVisibility != null ? !columnVisibility.equals(
          that.columnVisibility) : that.columnVisibility != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fieldsData != null ? fieldsData.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result +
                 (columnVisibility != null ? columnVisibility.hashCode() : 0);
        return result;
    }
}