package com.lucure.core;

import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class RestrictedField extends Field {

    public static Object toObject(Field field) {
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
      Object data = toObject(field);
      this.fieldsData = data;
      this.columnVisibility = columnVisibility;
  }

    public ColumnVisibility getColumnVisibility() {
    return columnVisibility;
  }

  @Override
  public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) throws IOException {
    TokenStream tokenStream = analyzer.tokenStream(name(), super.stringValue());
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
}