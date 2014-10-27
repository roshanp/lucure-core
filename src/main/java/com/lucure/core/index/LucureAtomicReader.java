package com.lucure.core.index;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.FilterAtomicReader;
import org.apache.lucene.index.StoredFieldVisitor;

import java.io.IOException;

/**
 */
public class LucureAtomicReader extends FilterAtomicReader {

    public LucureAtomicReader(AtomicReader atomicReader) {
        super(atomicReader);
    }

    @Override
    public void document(
      int docID, StoredFieldVisitor visitor) throws IOException {
        visitor = DelegatingRestrictedFieldVisitor.wrap(visitor);
        super.document(docID, visitor);
    }

}
