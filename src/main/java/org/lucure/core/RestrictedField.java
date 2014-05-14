package org.lucure.core;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.util.BytesRef;

public class RestrictedField extends Field {

  public static final char DELIM = '\u0000';

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
  private final String columnVisibilityStr;

  public RestrictedField(String name, String value, FieldType type,
                         ColumnVisibility columnVisibility) {
    super(name, value, type);
    this.columnVisibility = columnVisibility;
    this.columnVisibilityStr = new String(columnVisibility.getExpression(), Charset.forName("UTF-8"));
  }

  public ColumnVisibility getColumnVisibility() {
    return columnVisibility;
  }

  @Override
  public String stringValue() {
    //for the stored field visitor to parse based on CV, the stored field value
    //must have the CV
    return super.stringValue() + DELIM + columnVisibilityStr;
  }

  @Override
  public TokenStream tokenStream(Analyzer analyzer) throws IOException {
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