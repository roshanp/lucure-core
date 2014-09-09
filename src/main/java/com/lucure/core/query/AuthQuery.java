package com.lucure.core.query;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Query with embedded Authorizations
 */
public class AuthQuery extends Query {

    public static final ThreadLocal<Authorizations> threadAuthorizations =
      new ThreadLocal<Authorizations>() {
          @Override protected Authorizations initialValue() {
              return new Authorizations();
          }
      };

    private static class AuthWeight extends Weight {

        private final Weight weight;
        private final AuthQuery authQuery;
        private final Authorizations authorizations;

        private AuthWeight(Weight weight, AuthQuery authQuery, Authorizations authorizations) {
            this.weight = weight;
            this.authQuery = authQuery;
            this.authorizations = authorizations;
        }

        @Override
        public boolean scoresDocsOutOfOrder() {
            return weight.scoresDocsOutOfOrder();
        }

        @Override
        public Explanation explain(
          AtomicReaderContext context, int doc) throws IOException {
            return weight.explain(context, doc);
        }

        @Override
        public Query getQuery() {
            return authQuery;
        }

        @Override
        public float getValueForNormalization() throws IOException {
            return weight.getValueForNormalization();
        }

        @Override
        public void normalize(float norm, float topLevelBoost) {
            weight.normalize(norm, topLevelBoost);
        }

        @Override
        public Scorer scorer(
          AtomicReaderContext context, Bits acceptDocs) throws IOException {
            threadAuthorizations.set(authorizations);
            Scorer scorer = weight.scorer(context, acceptDocs);
            if(scorer == null) {
                return null;
            }

            return new AuthScorer(scorer, this, authorizations);
        }

        @Override
        public BulkScorer bulkScorer(
          AtomicReaderContext context, boolean scoreDocsInOrder,
          Bits acceptDocs) throws IOException {
            BulkScorer bulkScorer = super.bulkScorer(context, scoreDocsInOrder,
                                                     acceptDocs);
            if(bulkScorer == null) {
                return null; //no docs
            }

            return new BulkAuthScorer(bulkScorer, authorizations);
        }
    }

    private static class AuthScorer extends Scorer {

        private final Scorer scorer;
        private final Authorizations authorizations;

        private AuthScorer(
          Scorer scorer, Weight weight, Authorizations authorizations) {
            super(weight);
            this.scorer = scorer;
            this.authorizations = authorizations;
        }

        @Override
        public float score() throws IOException {
            threadAuthorizations.set(authorizations);
            return scorer.score();
        }

        @Override
        public Weight getWeight() {
            threadAuthorizations.set(authorizations);
            return scorer.getWeight();
        }

        @Override
        public Collection<ChildScorer> getChildren() {
            threadAuthorizations.set(authorizations);
            return scorer.getChildren();
        }

        @Override
        public int freq() throws IOException {
            threadAuthorizations.set(authorizations);
            return scorer.freq();
        }

        @Override
        public AttributeSource attributes() {
            threadAuthorizations.set(authorizations);
            return scorer.attributes();
        }

        @Override
        public int docID() {
            threadAuthorizations.set(authorizations);
            return scorer.docID();
        }

        @Override
        public int nextDoc() throws IOException {
            threadAuthorizations.set(authorizations);
            return scorer.nextDoc();
        }

        @Override
        public int advance(int target) throws IOException {
            threadAuthorizations.set(authorizations);
            return scorer.advance(target);
        }

        @Override
        public long cost() {
            threadAuthorizations.set(authorizations);
            return scorer.cost();
        }
    }

    private static class BulkAuthScorer extends BulkScorer{
        private final BulkScorer bulkScorer;
        private final Authorizations authorizations;

        private BulkAuthScorer(
          BulkScorer bulkScorer, Authorizations authorizations) {
            this.bulkScorer = bulkScorer;
            this.authorizations = authorizations;
        }

        @Override
        public boolean score(Collector collector, int max) throws IOException {
            threadAuthorizations.set(authorizations);
            return bulkScorer.score(collector, max);
        }
    }

    private final Query query;
    private final Authorizations authorizations;

    public AuthQuery(Query query, Authorizations authorizations) {
        this.query = query;
        this.authorizations = authorizations;
    }

    @Override
    public void setBoost(float b) {
        query.setBoost(b);
    }

    @Override
    public float getBoost() {
        return query.getBoost();
    }

    @Override
    public String toString(String field) {
        return query.toString(field);
    }

    @Override
    public String toString() {
        return query.toString();
    }

    @Override
    public Weight createWeight(IndexSearcher searcher) throws IOException {
        return new AuthWeight(query.createWeight(searcher), this, authorizations);
    }

    @Override
    public Query rewrite(
      IndexReader reader) throws IOException {
        Query rewrite = query.rewrite(reader);
        if(!query.equals(rewrite)) {
            return new AuthQuery(rewrite, authorizations);
        }
        return this;
    }

    @Override
    public void extractTerms(Set<Term> terms) {
        query.extractTerms(terms);
    }

    @Override
    public Query clone() {
        return new AuthQuery(query.clone(), new Authorizations(authorizations.getAuthorizations()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthQuery)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AuthQuery authQuery = (AuthQuery) o;

        if (authorizations != null ? !authorizations.equals(
          authQuery.authorizations) : authQuery.authorizations != null) {
            return false;
        }
        if (query != null ? !query.equals(authQuery.query) :
            authQuery.query != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result +
                 (authorizations != null ? authorizations.hashCode() : 0);
        return result;
    }
}
