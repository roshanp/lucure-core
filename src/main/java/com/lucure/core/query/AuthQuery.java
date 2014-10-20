package com.lucure.core.query;

import com.lucure.core.AuthorizationsHolder;
import com.lucure.core.security.Authorizations;
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

    private static class AuthWeight extends Weight {

        private final Weight weight;
        private final AuthQuery authQuery;
        private final AuthorizationsHolder authorizationsHolder;

        private AuthWeight(Weight weight, AuthQuery authQuery, AuthorizationsHolder authorizationsHolder) {
            this.weight = weight;
            this.authQuery = authQuery;
            this.authorizationsHolder = authorizationsHolder;
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
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            Scorer scorer = weight.scorer(context, acceptDocs);
            if(scorer == null) {
                return null;
            }

            return new AuthScorer(scorer, this, authorizationsHolder);
        }

        @Override
        public BulkScorer bulkScorer(
          AtomicReaderContext context, boolean scoreDocsInOrder,
          Bits acceptDocs) throws IOException {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            BulkScorer bulkScorer = super.bulkScorer(context, scoreDocsInOrder,
                                                     acceptDocs);
            if(bulkScorer == null) {
                return null; //no docs
            }

            return new BulkAuthScorer(bulkScorer, authorizationsHolder);
        }
    }

    private static class AuthScorer extends Scorer {

        private final Scorer scorer;
        private final AuthorizationsHolder authorizationsHolder;

        private AuthScorer(
          Scorer scorer, Weight weight, AuthorizationsHolder authorizationsHolder) {
            super(weight);
            this.scorer = scorer;
            this.authorizationsHolder = authorizationsHolder;
        }

        @Override
        public float score() throws IOException {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.score();
        }

        @Override
        public Weight getWeight() {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.getWeight();
        }

        @Override
        public Collection<ChildScorer> getChildren() {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.getChildren();
        }

        @Override
        public int freq() throws IOException {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.freq();
        }

        @Override
        public AttributeSource attributes() {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.attributes();
        }

        @Override
        public int docID() {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.docID();
        }

        @Override
        public int nextDoc() throws IOException {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.nextDoc();
        }

        @Override
        public int advance(int target) throws IOException {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.advance(target);
        }

        @Override
        public long cost() {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return scorer.cost();
        }
    }

    private static class BulkAuthScorer extends BulkScorer{
        private final BulkScorer bulkScorer;
        private final AuthorizationsHolder authorizationsHolder;

        private BulkAuthScorer(
          BulkScorer bulkScorer, AuthorizationsHolder authorizationsHolder) {
            this.bulkScorer = bulkScorer;
            this.authorizationsHolder = authorizationsHolder;
        }

        @Override
        public boolean score(Collector collector, int max) throws IOException {
            AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
            return bulkScorer.score(collector, max);
        }
    }

    private final Query query;
    private final AuthorizationsHolder authorizationsHolder;

    public AuthQuery(Query query, Authorizations authorizations) {
        this.query = query;
        this.authorizationsHolder = new AuthorizationsHolder(authorizations);
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
        return new AuthWeight(query.createWeight(searcher), this, authorizationsHolder);
    }

    @Override
    public Query rewrite(
      IndexReader reader) throws IOException {
        Query rewrite = query.rewrite(reader);
        if(!query.equals(rewrite)) {
            return new AuthQuery(rewrite, authorizationsHolder.getAuthorizations());
        }
        return this;
    }

    @Override
    public void extractTerms(Set<Term> terms) {
        query.extractTerms(terms);
    }

    @Override
    public Query clone() {
        return new AuthQuery(query.clone(), new Authorizations(
          authorizationsHolder.getAuthorizations().getAuthorizations()));
    }

    public Query getInnerQuery() {
        return query;
    }

    public Authorizations getAuthorizations() {
        return authorizationsHolder.getAuthorizations();
    }

    public void loadCurrentAuthorizations() {
        AuthorizationsHolder.threadAuthorizations.set(authorizationsHolder);
    }

    public void clearCurrentAuthorizations() {
        AuthorizationsHolder.threadAuthorizations.remove();
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

        if (authorizationsHolder != null ? !authorizationsHolder.equals(
          authQuery.authorizationsHolder) : authQuery.authorizationsHolder != null) {
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
                 (authorizationsHolder != null ? authorizationsHolder.hashCode() : 0);
        return result;
    }
}
