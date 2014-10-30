package com.lucure.core;

import com.lucure.core.security.FieldVisibility;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

import static com.lucure.core.util.IndexableFieldUtils.from;
import static com.lucure.core.util.IndexableFieldUtils.toObject;

/**
 * A RestrictedField provides a {@link com.lucure.core.security.FieldVisibility} over
 * with a field. The visibility is encoded into the payload of the tokenStream when tokenized
 */
public class RestrictedField extends Field {

    public static final class ColumnVisibilityPayloadFilter
      extends TokenFilter {
        private final PayloadAttribute payAtt = addAttribute(
          PayloadAttribute.class);
        private final FieldVisibility fieldVisibility;

        public ColumnVisibilityPayloadFilter(
          TokenStream input, FieldVisibility fieldVisibility) {
            super(input);
            this.fieldVisibility = fieldVisibility;
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (input.incrementToken()) {
                payAtt
                  .setPayload(new BytesRef(fieldVisibility.getExpression()));
                return true;
            } else {
                return false;
            }
        }
    }

    private final FieldVisibility fieldVisibility;

    public RestrictedField(
      String name, Object value, FieldType type,
      FieldVisibility fieldVisibility) {
        super(name, type);
        this.fieldsData = value;
        this.fieldVisibility = fieldVisibility;
    }

    public RestrictedField(Field field, FieldVisibility fieldVisibility) {
        super(field.name(), field.fieldType());
        this.fieldsData = toObject(field);
        this.fieldVisibility = fieldVisibility;
    }

    public RestrictedField(IndexableField field, FieldVisibility fieldVisibility) {
        super(field.name(), from(field.fieldType()));
        this.fieldsData = toObject(field);
        this.fieldVisibility = fieldVisibility;
    }

    public FieldVisibility getFieldVisibility() {
        return fieldVisibility;
    }

    @Override
    public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse)
      throws IOException {
        TokenStream tokenStream = super.tokenStream(analyzer, reuse);
        tokenStream = new ColumnVisibilityPayloadFilter(tokenStream, fieldVisibility);
        return tokenStream;
    }

    @Override
    public String toString() {
        return "RestrictedField{" +
               "super=" + super.toString() +
               ", columnVisibility=" + fieldVisibility +
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
        if (fieldsData != null ? !fieldsData.equals(that.fieldsData) :
            that.fieldsData != null) {
            return false;
        }
        if (fieldVisibility != null ?
            !fieldVisibility.equals(that.fieldVisibility) :
            that.fieldVisibility != null) {
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
                 (fieldVisibility != null ? fieldVisibility.hashCode() : 0);
        return result;
    }
}