package com.lucure.core.codec;

import com.lucure.core.AuthorizationsHolder;
import com.lucure.core.security.Authorizations;
import com.lucure.core.security.ColumnVisibility;
import com.lucure.core.security.VisibilityEvaluator;
import com.lucure.core.security.VisibilityParseException;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Arrays;

import static com.lucure.core.codec.AccessFilteredDocsAndPositionsEnum
  .AllAuthorizationsHolder.ALLAUTHSHOLDER;

/**
 * Enum to read and restrict access to a document based on the payload which
 * is expected to store the visibility
 */
public class AccessFilteredDocsAndPositionsEnum extends DocsAndPositionsEnum {

    /**
     * This placeholder allows for lucene specific operations such as
     * merge to read data with all authorizations enabled. This should never
     * be used outside of the Codec.
     */
    static class AllAuthorizationsHolder extends AuthorizationsHolder {

        static final AllAuthorizationsHolder ALLAUTHSHOLDER = new AllAuthorizationsHolder();

        private AllAuthorizationsHolder() {
            super(Authorizations.EMPTY);
        }
    }

    static void enableMergeAuthorizations() {
        AuthorizationsHolder.threadAuthorizations.set(ALLAUTHSHOLDER);
    }

    static void disableMergeAuthorizations() {
        AuthorizationsHolder.threadAuthorizations.remove();
    }

    private final DocsAndPositionsEnum docsAndPositionsEnum;
    private final AuthorizationsHolder authorizationsHolder;

    public AccessFilteredDocsAndPositionsEnum(
      DocsAndPositionsEnum docsAndPositionsEnum) {
        this(docsAndPositionsEnum, AuthorizationsHolder.threadAuthorizations.get());
    }

    public AccessFilteredDocsAndPositionsEnum(
      DocsAndPositionsEnum docsAndPositionsEnum,
      AuthorizationsHolder authorizationsHolder) {
        this.docsAndPositionsEnum = docsAndPositionsEnum;
        this.authorizationsHolder = authorizationsHolder;
    }

    @Override
    public int nextPosition() throws IOException {
        return docsAndPositionsEnum.nextPosition();
    }

    @Override
    public int startOffset() throws IOException {
        return docsAndPositionsEnum.startOffset();
    }

    @Override
    public int endOffset() throws IOException {
        return docsAndPositionsEnum.endOffset();
    }

    @Override
    public BytesRef getPayload() throws IOException {
        return docsAndPositionsEnum.getPayload();
    }

    @Override
    public int freq() throws IOException {
        return docsAndPositionsEnum.freq();
    }

    @Override
    public int docID() {
        return docsAndPositionsEnum.docID();
    }

    @Override
    public int nextDoc() throws IOException {
        try {
            while (docsAndPositionsEnum.nextDoc() != NO_MORE_DOCS) {
                if (hasAccess()) {
                    return docID();
                }
            }
            return NO_MORE_DOCS;
        } catch (VisibilityParseException vpe) {
            throw new IOException("Exception occurred parsing visibility", vpe);
        }
    }

    @Override
    public int advance(int target) throws IOException {
        int advance = docsAndPositionsEnum.advance(target);
        if (advance != NO_MORE_DOCS) {
            try {
                if (hasAccess()) {
                    return docID();
                } else {
                    //seek to next available
                    int doc;
                    while ((doc = nextDoc()) < target) {
                    }
                    return doc;
                }
            } catch (VisibilityParseException vpe) {
                throw new IOException("Exception occurred parsing visibility",
                                      vpe);
            }
        }
        return NO_MORE_DOCS;
    }

    @Override
    public long cost() {
        return docsAndPositionsEnum.cost();
    }

    protected boolean hasAccess() throws IOException, VisibilityParseException {
        docsAndPositionsEnum.nextPosition();
        BytesRef payload = docsAndPositionsEnum.getPayload();
        return payload == null ||
               ALLAUTHSHOLDER.equals(authorizationsHolder) ||
               this.authorizationsHolder.getVisibilityEvaluator().evaluate(
                 new ColumnVisibility(Arrays.copyOfRange(payload.bytes,
                                                         payload.offset,
                                                         payload.offset +
                                                         payload.length)));
    }

    @Override
    public AttributeSource attributes() {
        return super.attributes();
    }
}