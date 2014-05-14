package org.lucure.core;

import java.io.IOException;
import java.util.Collection;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
import org.apache.accumulo.core.security.VisibilityParseException;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.spans.TermSpans;
import org.apache.lucene.util.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * TermQuery that will restrict query on underlying payload Visibility
 */
public class AuthTermQuery extends Query {

  private static final Logger LOG = LoggerFactory.getLogger(AuthTermQuery.class);

  public static class AuthTermWeight extends Weight {

    private final AuthTermQuery authTermQuery;
    private final Weight termWeight;

    public AuthTermWeight(Weight termWeight, AuthTermQuery authTermQuery)
        throws IOException {
      super();
      this.termWeight = termWeight;
      this.authTermQuery = authTermQuery;
    }

    @Override
    public Explanation explain(AtomicReaderContext context, int doc)
        throws IOException {
      //TODO: Fill in
      return null;
    }

    @Override
    public Query getQuery() {
      return authTermQuery;
    }

    @Override
    public float getValueForNormalization() throws IOException {
      return termWeight.getValueForNormalization();
    }

    @Override
    public void normalize(float norm, float topLevelBoost) {
      termWeight.normalize(norm, topLevelBoost);
    }

    @Override
    public Scorer scorer(AtomicReaderContext context, boolean scoreDocsInOrder,
                         boolean topScorer, Bits acceptDocs)
        throws IOException {
      TermsEnum termsEnum = getTermsEnum(context);
      DocsAndPositionsEnum docsAndPositionsEnum = termsEnum
          .docsAndPositions(acceptDocs, null,
              DocsAndPositionsEnum.FLAG_PAYLOADS);
      if (docsAndPositionsEnum == null) {
        throw new IOException("Field not indexed with docs and positions");
      }
      return new AuthScorer(authTermQuery, termWeight, docsAndPositionsEnum);
    }

    private TermsEnum getTermsEnum(AtomicReaderContext context)
        throws IOException {
      Term term = authTermQuery.term;
      final TermsEnum termsEnum =
          context.reader().fields().terms(term.field()).iterator(null);
      termsEnum.seekExact(term.bytes());
      return termsEnum;
    }

  }

  public static class AuthScorer extends Scorer {

    private final AuthTermQuery authTermQuery;
    private final TermSpans termSpans;
    float currScore = 0.0f;

    public AuthScorer(AuthTermQuery authTermQuery, Weight weight,
                      DocsAndPositionsEnum docsAndPositionsEnum)
        throws IOException {
      super(weight);
      this.authTermQuery = authTermQuery;
      this.termSpans = new TermSpans(docsAndPositionsEnum, authTermQuery.term);
    }

    @Override
    public float score() throws IOException {
      return currScore;
    }

    @Override
    public int freq() throws IOException {
      return termSpans.getPostings().freq();
    }

    @Override
    public int docID() {
      return termSpans.doc();
    }

    @Override
    public int nextDoc() throws IOException {
      try {
        while (termSpans.next()) {
          if (hasAccess()) {
            return termSpans.doc();
          }
        }
        return NO_MORE_DOCS;
      } catch (VisibilityParseException vpe) {
        //TODO: log
        throw new IOException("Exception occurred parsing visibility", vpe);
      }
    }

    @Override
    public int advance(int target) throws IOException {
      try {
        if (termSpans.skipTo(target)) {
          do {
            if (hasAccess()) {
              return termSpans.doc();
            }
          } while (termSpans.next());
        }
        return NO_MORE_DOCS;
      } catch (VisibilityParseException e) {
        //TODO: log
        throw new IOException("Exception occurred parsing visibility", e);
      }
    }

    public boolean hasAccess() throws IOException, VisibilityParseException {
      if (termSpans.isPayloadAvailable()) {
        Collection<byte[]> payloads = termSpans.getPayload();
        byte[] payload = Iterables.getFirst(payloads, null);
        if (payload != null) {
          //restrict if visibility present
          return authTermQuery.visibilityEvaluator
              .evaluate(new ColumnVisibility(payload));
        }
      }
      return true; //no visibility set
    }

    @Override
    public long cost() {
      return termSpans.cost();
    }

  }

  private final Term term;
  private final TermQuery termQuery;
  private final Authorizations authorizations;
  private final VisibilityEvaluator visibilityEvaluator;

  public AuthTermQuery(Term term, Authorizations authorizations) {
    this.term = term;
    this.termQuery = new TermQuery(term);
    this.authorizations = authorizations;
    this.visibilityEvaluator = new VisibilityEvaluator(authorizations);
  }

  @Override
  public Weight createWeight(IndexSearcher searcher) throws IOException {
    return new AuthTermWeight(termQuery.createWeight(searcher), this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    AuthTermQuery that = (AuthTermQuery) o;

    if (authorizations != null ? !authorizations.equals(that.authorizations) :
        that.authorizations != null) return false;
    if (term != null ? !term.equals(that.term) : that.term != null)
      return false;
    if (termQuery != null ? !termQuery.equals(that.termQuery) :
        that.termQuery != null) return false;
    if (visibilityEvaluator != null ?
        !visibilityEvaluator.equals(that.visibilityEvaluator) :
        that.visibilityEvaluator != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (term != null ? term.hashCode() : 0);
    result = 31 * result + (termQuery != null ? termQuery.hashCode() : 0);
    result =
        31 * result + (authorizations != null ? authorizations.hashCode() : 0);
    result = 31 * result +
        (visibilityEvaluator != null ? visibilityEvaluator.hashCode() : 0);
    return result;
  }

  @Override
  public String toString(String field) {
    return "AuthTermQuery: " + termQuery.toString();
  }
}
