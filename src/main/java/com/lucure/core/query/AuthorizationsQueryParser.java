package com.lucure.core.query;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AuthorizationsQueryParser extends QueryParser {
    private static Logger LOG = LoggerFactory.getLogger(
      AuthorizationsQueryParser.class);

    private final Authorizations authorizations;

    public AuthorizationsQueryParser(
      Version matchVersion, String f, Analyzer a,
      Authorizations authorizations) {
        super(matchVersion, f, a);
        this.authorizations = authorizations;
    }

    @Override
    protected Query newTermQuery(Term term) {
        return new AuthQuery(super.newTermQuery(term), authorizations);
    }

    @Override
    protected Query newMatchAllDocsQuery() {
        return new AuthQuery(super.newMatchAllDocsQuery(), authorizations);
    }

    @Override
    protected Query newFieldQuery(
      Analyzer analyzer, String field, String queryText, boolean quoted)
      throws ParseException {
        return new AuthQuery(super.newFieldQuery(analyzer, field, queryText, quoted), authorizations);
    }

    @Override
    protected BooleanClause newBooleanClause(
      Query q, BooleanClause.Occur occur) {
        return super.newBooleanClause(new AuthQuery(q, authorizations), occur);
    }

    @Override
    protected Query newPrefixQuery(
      Term prefix) {
        return new AuthQuery(super.newPrefixQuery(prefix), authorizations);
    }

    @Override
    protected Query newFuzzyQuery(
      Term term, float minimumSimilarity, int prefixLength) {
        return new AuthQuery(super.newFuzzyQuery(term, minimumSimilarity, prefixLength), authorizations);
    }

    @Override
    protected Query newRegexpQuery(Term regexp) {
        return new AuthQuery(super.newRegexpQuery(regexp), authorizations);
    }

    @Override
    protected Query newRangeQuery(
      String field, String part1, String part2, boolean startInclusive,
      boolean endInclusive) {
        return new AuthQuery(super.newRangeQuery(field, part1, part2, startInclusive,
                                   endInclusive), authorizations);
    }

    @Override
    protected Query newWildcardQuery(Term t) {
        return new AuthQuery(super.newWildcardQuery(t), authorizations);
    }

}
