package com.lucure.core;

import com.lucure.core.query.AuthQuery;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 */
public class LucureIndexSearcher extends IndexSearcher {
    public LucureIndexSearcher(IndexReader r) {
        super(r);
    }

    public LucureIndexSearcher(
      IndexReader r, ExecutorService executor) {
        super(r, executor);
    }

    public LucureIndexSearcher(
      IndexReaderContext context, ExecutorService executor) {
        super(context, executor);
    }

    public LucureIndexSearcher(IndexReaderContext context) {
        super(context);
    }

    public Document doc(int docID, Authorizations authorizations) throws IOException {
        AuthorizationsHolder.threadAuthorizations.set(new AuthorizationsHolder(authorizations));
        return doc(docID);
    }

    /**
     * Sugar for <code>.getIndexReader().document(docID, fieldVisitor)</code>
     * @see IndexReader#document(int, org.apache.lucene.index.StoredFieldVisitor)
     */
    public void doc(int docID, StoredFieldVisitor fieldVisitor, Authorizations authorizations) throws IOException {
        AuthorizationsHolder.threadAuthorizations.set(new AuthorizationsHolder(authorizations));
        doc(docID, fieldVisitor);
    }

    /**
     * Sugar for <code>.getIndexReader().document(docID, fieldsToLoad)</code>
     * @see IndexReader#document(int, java.util.Set)
     */
    public Document doc(int docID, Set<String> fieldsToLoad, Authorizations authorizations) throws IOException {
        AuthorizationsHolder.threadAuthorizations.set(new AuthorizationsHolder(authorizations));
        return doc(docID, fieldsToLoad);
    }

    
    public TopDocs searchAfter(
      ScoreDoc after, Query query, int n, Authorizations authorizations) throws IOException {
        return super.searchAfter(after, new AuthQuery(query, authorizations), n);
    }


    public TopDocs searchAfter(
      ScoreDoc after, Query query, Filter filter, int n, Authorizations authorizations) throws IOException {
        return super.searchAfter(after, new AuthQuery(query, authorizations), filter, n);
    }


    public TopDocs search(
      Query query, int n, Authorizations authorizations) throws IOException {
        return super.search(new AuthQuery(query, authorizations), n);
    }


    public TopDocs search(
      Query query, Filter filter, int n, Authorizations authorizations) throws IOException {
        return super.search(new AuthQuery(query, authorizations), filter, n);
    }


    public void search(
      Query query, Filter filter, Collector results, Authorizations authorizations) throws IOException {
        super.search(new AuthQuery(query, authorizations), filter, results);
    }


    public void search(
      Query query, Collector results, Authorizations authorizations) throws IOException {
        super.search(new AuthQuery(query, authorizations), results);
    }


    public TopFieldDocs search(
      Query query, Filter filter, int n, Sort sort, Authorizations authorizations) throws IOException {
        return super.search(new AuthQuery(query, authorizations), filter, n, sort);
    }


    public TopFieldDocs search(
      Query query, Filter filter, int n, Sort sort, boolean doDocScores,
      boolean doMaxScore, Authorizations authorizations) throws IOException {
        return super.search(new AuthQuery(query, authorizations), filter, n, sort, doDocScores, doMaxScore);
    }


    public TopDocs searchAfter(
      ScoreDoc after, Query query, Filter filter, int n, Sort sort, Authorizations authorizations)
      throws IOException {
        return super.searchAfter(after, new AuthQuery(query, authorizations), filter, n, sort);
    }


    public TopFieldDocs search(
      Query query, int n, Sort sort, Authorizations authorizations) throws IOException {
        return super.search(new AuthQuery(query, authorizations), n, sort);
    }


    public TopDocs searchAfter(
      ScoreDoc after, Query query, int n, Sort sort, Authorizations authorizations) throws IOException {
        return super.searchAfter(after, new AuthQuery(query, authorizations), n, sort);
    }


    public TopDocs searchAfter(
      ScoreDoc after, Query query, Filter filter, int n, Sort sort,
      boolean doDocScores, boolean doMaxScore, Authorizations authorizations) throws IOException {
        return super.searchAfter(after, new AuthQuery(query, authorizations), filter, n, sort, doDocScores,
                                 doMaxScore);
    }
}
