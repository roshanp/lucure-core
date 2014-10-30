package com.lucure.core;

import com.lucure.core.security.FieldVisibility;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.junit.Test;

import static org.junit.Assert.*;

public class RestrictedFieldTest {

    @Test
    public void testColumnVisibilityPayload() throws Exception {
        String visibility = "U";
        String value = "value";
        RestrictedField restrictedField = new RestrictedField(new StringField(
          "field", value, Field.Store.NO), new FieldVisibility(visibility));
        try(TokenStream tokenStream = restrictedField.tokenStream(
          new WhitespaceAnalyzer(), null)) {
            CharTermAttribute charTermAttribute = tokenStream
              .getAttribute(CharTermAttribute.class);
            PayloadAttribute payloadAttribute = tokenStream
              .getAttribute(PayloadAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                assertEquals(value, new String(charTermAttribute.buffer(), 0, charTermAttribute.length()));
                assertEquals(visibility, new String(payloadAttribute.getPayload().bytes));
            }
        }
    }

    @Test
    public void testEquals() throws Exception {
        String visibility = "U";
        String value = "value";
        RestrictedField restrictedField = new RestrictedField(new StringField(
          "field", value, Field.Store.NO), new FieldVisibility(visibility));
        assertTrue(restrictedField.equals(restrictedField));
        assertTrue(restrictedField.equals(new RestrictedField(new StringField(
          "field", value, Field.Store.NO), new FieldVisibility(visibility))));
        assertFalse(restrictedField.equals(new RestrictedField(new StringField(
          "field1", value, Field.Store.NO), new FieldVisibility(visibility))));
        assertFalse(restrictedField.equals(new RestrictedField(new StringField(
          "field", value, Field.Store.YES), new FieldVisibility(visibility))));
        assertFalse(restrictedField.equals(new RestrictedField(new StringField(
          "field", "notVal", Field.Store.NO), new FieldVisibility(visibility))));
        assertFalse(restrictedField.equals(new RestrictedField(new StringField(
          "field", value, Field.Store.NO), new FieldVisibility("U&FOUO"))));
    }
}