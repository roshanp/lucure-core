package com.lucure.core.query;

import com.lucure.core.security.Authorizations;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lucene {@link QueryParser} to produce {@link AuthQuery}
 */
public class AuthorizationsQueryParser extends QueryParser {
    private static Logger LOG = LoggerFactory.getLogger(AuthorizationsQueryParser.class);

    public static final class AuthQueryFactory {

        protected final Authorizations authorizations;

        public AuthQueryFactory(Authorizations authorizations) {
            this.authorizations = authorizations;
        }

        public AuthQuery decorate(Query query) {
            return new AuthQuery(query, authorizations);
        }
    }

    private final AuthQueryFactory authQueryFactory;

    public AuthorizationsQueryParser(
      String f, Analyzer a, Authorizations authorizations) {
        this(f, a, new AuthQueryFactory(authorizations));
    }

    public AuthorizationsQueryParser(
      String f, Analyzer a, AuthQueryFactory authQueryFactory) {
        super(f, a);
        this.authQueryFactory = authQueryFactory;
    }

    @Override
    protected Query newTermQuery(Term term) {
        return authQueryFactory.decorate(super.newTermQuery(term));
    }

    @Override
    protected Query newMatchAllDocsQuery() {
        return authQueryFactory.decorate(super.newMatchAllDocsQuery());
    }

    @Override
    protected Query newFieldQuery(
      Analyzer analyzer, String field, String queryText, boolean quoted) throws ParseException {
        return authQueryFactory.decorate(super.newFieldQuery(analyzer, field, queryText, quoted));
    }

    @Override
    protected BooleanClause newBooleanClause(
      Query q, BooleanClause.Occur occur) {
        return super.newBooleanClause(authQueryFactory.decorate(q), occur);
    }

    @Override
    protected Query newPrefixQuery(
      Term prefix) {
        return authQueryFactory.decorate(super.newPrefixQuery(prefix));
    }

    @Override
    protected Query newFuzzyQuery(
      Term term, float minimumSimilarity, int prefixLength) {
        return authQueryFactory
          .decorate(super.newFuzzyQuery(term, minimumSimilarity, prefixLength));
    }

    @Override
    protected Query newRegexpQuery(Term regexp) {
        return authQueryFactory.decorate(super.newRegexpQuery(regexp));
    }

    @Override
    protected Query newRangeQuery(
      String field, String part1, String part2, boolean startInclusive, boolean endInclusive) {
        return authQueryFactory
          .decorate(super.newRangeQuery(field, part1, part2, startInclusive, endInclusive));
    }

    @Override
    protected Query newWildcardQuery(Term t) {
        return authQueryFactory.decorate(super.newWildcardQuery(t));
    }

    @Override
    protected Query getBooleanQuery(
      List<BooleanClause> clauses) throws ParseException {
        return authQueryFactory.decorate(super.getBooleanQuery(clauses));
    }

    @Override
    protected Query getFieldQuery(
      String field, String queryText, boolean quoted) throws ParseException {
        return authQueryFactory.decorate(super.getFieldQuery(field, queryText, quoted));
    }

    @Override
    protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
        return authQueryFactory.decorate(super.getFieldQuery(field, queryText, slop));
    }

    @Override
    protected Query getRangeQuery(
      String field, String part1, String part2, boolean startInclusive, boolean endInclusive)
      throws ParseException {
        return authQueryFactory
          .decorate(super.getRangeQuery(field, part1, part2, startInclusive, endInclusive));
    }

    @Override
    protected Query getFuzzyQuery(
      String field, String termStr, float minSimilarity) throws ParseException {
        return authQueryFactory.decorate(super.getFuzzyQuery(field, termStr, minSimilarity));
    }

    @Override
    protected Query getPrefixQuery(String field, String termStr) throws ParseException {
        return authQueryFactory.decorate(super.getPrefixQuery(field, termStr));
    }

    @Override
    protected Query getRegexpQuery(String field, String termStr) throws ParseException {
        return authQueryFactory.decorate(super.getRegexpQuery(field, termStr));
    }

    @Override
    protected Query getWildcardQuery(
      String field, String termStr) throws ParseException {
        return authQueryFactory.decorate(super.getWildcardQuery(field, termStr));
    }

    @Override
    protected Query getBooleanQuery(
      List<BooleanClause> clauses, boolean disableCoord) throws ParseException {
        return authQueryFactory.decorate(super.getBooleanQuery(clauses, disableCoord));
    }
}
