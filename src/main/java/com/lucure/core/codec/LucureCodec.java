package com.lucure.core.codec;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.codecs.*;
import org.apache.lucene.codecs.lucene40.Lucene40LiveDocsFormat;
import org.apache.lucene.codecs.lucene42.Lucene42TermVectorsFormat;
import org.apache.lucene.codecs.lucene46.Lucene46FieldInfosFormat;
import org.apache.lucene.codecs.lucene46.Lucene46SegmentInfoFormat;
import org.apache.lucene.codecs.lucene49.Lucene49NormsFormat;
import org.apache.lucene.codecs.perfield.PerFieldPostingsFormat;
import org.apache.lucene.index.*;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * Implements the Lucene 4.6 index format, with configurable per-field postings
 * and docvalues formats.
 * <p>
 * If you want to reuse functionality of this codec in another codec, extend
 * {@link FilterCodec}.
 *
 * @see org.apache.lucene.codecs.lucene46 package documentation for file format details.
 * @lucene.experimental
 */
// NOTE: if we make largish changes in a minor release, easier to just make Lucene46Codec or whatever
// if they are backwards compatible or smallish we can probably do the backwards in the postingsreader
// (it writes a minor version, etc).
public class LucureCodec extends Codec {
    public static final String LUCURE_CODEC_NAME = "Lucure";
    private final StoredFieldsFormat fieldsFormat = new LucureStoredFieldsFormat();
  private final TermVectorsFormat vectorsFormat = new Lucene42TermVectorsFormat();
  private final FieldInfosFormat fieldInfosFormat = new Lucene46FieldInfosFormat();
  private final SegmentInfoFormat segmentInfosFormat = new Lucene46SegmentInfoFormat();
  private final LiveDocsFormat liveDocsFormat = new Lucene40LiveDocsFormat();

  /** Sole constructor. */
  public LucureCodec() {
    super(LUCURE_CODEC_NAME);
  }
  
  @Override
  public final StoredFieldsFormat storedFieldsFormat() {
    return fieldsFormat;
  }
  
  @Override
  public final TermVectorsFormat termVectorsFormat() {
    return vectorsFormat;
  }

  @Override
  public final PostingsFormat postingsFormat() {
    return defaultFormat;
  }
  
  @Override
  public final FieldInfosFormat fieldInfosFormat() {
    return fieldInfosFormat;
  }
  
  @Override
  public final SegmentInfoFormat segmentInfoFormat() {
    return segmentInfosFormat;
  }
  
  @Override
  public final LiveDocsFormat liveDocsFormat() {
    return liveDocsFormat;
  }
  
  @Override
  public final DocValuesFormat docValuesFormat() {
    return new DocValuesFormat(LUCURE_CODEC_NAME) {

        @Override
        public DocValuesConsumer fieldsConsumer(
          SegmentWriteState state) throws IOException {
            return new DocValuesConsumer() {

                @Override
                public void addNumericField(
                  FieldInfo field, Iterable<Number> values) throws IOException {

                }

                @Override
                public void addBinaryField(
                  FieldInfo field, Iterable<BytesRef> values)
                  throws IOException {

                }

                @Override
                public void addSortedField(
                  FieldInfo field, Iterable<BytesRef> values,
                  Iterable<Number> docToOrd) throws IOException {

                }

                @Override
                public void addSortedNumericField(
                  FieldInfo field, Iterable<Number> docToValueCount,
                  Iterable<Number> values) throws IOException {

                }

                @Override
                public void addSortedSetField(
                  FieldInfo field, Iterable<BytesRef> values,
                  Iterable<Number> docToOrdCount, Iterable<Number> ords)
                  throws IOException {

                }

                @Override
                public void close() throws IOException {

                }
            };
        }

        @Override
        public DocValuesProducer fieldsProducer(
          SegmentReadState state) throws IOException {
            return new DocValuesProducer() {
                @Override
                public NumericDocValues getNumeric(
                  FieldInfo field) throws IOException {
                    return null;
                }

                @Override
                public BinaryDocValues getBinary(
                  FieldInfo field) throws IOException {
                    return null;
                }

                @Override
                public SortedDocValues getSorted(
                  FieldInfo field) throws IOException {
                    return null;
                }

                @Override
                public SortedNumericDocValues getSortedNumeric(
                  FieldInfo field) throws IOException {
                    return null;
                }

                @Override
                public SortedSetDocValues getSortedSet(
                  FieldInfo field) throws IOException {
                    return null;
                }

                @Override
                public Bits getDocsWithField(
                  FieldInfo field) throws IOException {
                    return null;
                }

                @Override
                public void checkIntegrity() throws IOException {

                }

                @Override
                public long ramBytesUsed() {
                    return 0;
                }

                @Override
                public void close() throws IOException {

                }
            };
        }
    };
  }

  private final PostingsFormat defaultFormat = PostingsFormat.forName(
    LUCURE_CODEC_NAME);

  private final NormsFormat normsFormat = new Lucene49NormsFormat();

  @Override
  public final NormsFormat normsFormat() {
    return normsFormat;
  }
}
