package com.lucure.core;

import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
import org.apache.accumulo.core.security.VisibilityParseException;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import static org.apache.lucene.index.StoredFieldVisitor.Status.*;

/**
 */
public abstract class RestrictedStoredFieldVisitor extends StoredFieldVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(RestrictedStoredFieldVisitor.class);

    public static final String CV_ATTR_NAME = "cv";
    public static final ColumnVisibility EMPTY = new ColumnVisibility();

    private final VisibilityEvaluator visibilityEvaluator;

    public RestrictedStoredFieldVisitor(
      VisibilityEvaluator visibilityEvaluator) {
        this.visibilityEvaluator = visibilityEvaluator;
    }

    @Override
    public void binaryField(FieldInfo fieldInfo, byte[] value) throws
                                                               IOException {
        binaryField(fieldInfo, value, EMPTY);
    }

    public abstract void binaryField(
      FieldInfo fieldInfo, byte[] value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void stringField(FieldInfo fieldInfo, String value) throws IOException {
        stringField(fieldInfo, value, EMPTY);
    }

    public abstract void stringField(
      FieldInfo fieldInfo, String value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void intField(FieldInfo fieldInfo, int value) throws IOException {
        intField(fieldInfo, value, EMPTY);
    }

    public abstract void intField(
      FieldInfo fieldInfo, int value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void longField(FieldInfo fieldInfo, long value) throws IOException {
        longField(fieldInfo, value, EMPTY);
    }

    public abstract void longField(
      FieldInfo fieldInfo, long value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void floatField(FieldInfo fieldInfo, float value)
      throws IOException {
        floatField(fieldInfo, value, EMPTY);
    }

    public abstract void floatField(
      FieldInfo fieldInfo, float value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public void doubleField(FieldInfo fieldInfo, double value)
      throws IOException {
        doubleField(fieldInfo, value, EMPTY);
    }

    public abstract void doubleField(
      FieldInfo fieldInfo, double value, ColumnVisibility columnVisibility)
      throws IOException;

    @Override
    public Status needsField(FieldInfo fieldInfo) throws IOException {
        ColumnVisibility columnVisibility = getColumnVisibility(fieldInfo);
        return needsField(fieldInfo, columnVisibility);
    }

    public Status needsField(
      FieldInfo fieldInfo, ColumnVisibility columnVisibility)
      throws IOException {
        boolean hasAccess = true;
        try{
            hasAccess = visibilityEvaluator.evaluate(columnVisibility);
        } catch (VisibilityParseException e) {
            LOG.warn("Exception occurred parsing column visibility["+columnVisibility+"]", e);

        }
        return hasAccess ? YES : NO;
    }

    private ColumnVisibility getColumnVisibility(FieldInfo fieldInfo) {
        Map<String, String> attributes = fieldInfo.attributes();
        ColumnVisibility cv = EMPTY;
        if(attributes != null) {
            String cv_str = attributes.get(CV_ATTR_NAME);
            if(cv_str != null) {
                try {
                    cv = new ColumnVisibility(cv_str);
                } catch(PatternSyntaxException pe) {
                    //ignore unparseable
                    LOG.warn("Unparseable Column Visibility["+cv_str+"]", pe);
                }
            }
        }
        return cv;
    }
}
