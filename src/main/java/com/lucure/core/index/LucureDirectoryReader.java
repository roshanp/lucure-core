package com.lucure.core.index;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FilterDirectoryReader;

/**
 * Lucure version of the DirectoryReader in lucene
 */
public class LucureDirectoryReader extends FilterDirectoryReader {

    public LucureDirectoryReader(DirectoryReader in) {
        super(in, new SubReaderWrapper() {
            @Override
            public AtomicReader wrap(
              AtomicReader reader) {
                return new LucureAtomicReader(reader);
            }
        });
    }


    @Override
    protected DirectoryReader doWrapDirectoryReader(
      DirectoryReader in) {
        return new LucureDirectoryReader(in);
    }
}
