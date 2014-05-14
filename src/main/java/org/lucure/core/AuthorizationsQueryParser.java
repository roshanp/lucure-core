package org.lucure.core;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AuthorizationsQueryParser extends QueryParser{
  private static Logger LOG =
      LoggerFactory.getLogger(AuthorizationsQueryParser.class);

  private final Authorizations authorizations;

  public AuthorizationsQueryParser(Version matchVersion, String f, Analyzer a,
                                   Authorizations authorizations) {
    super(matchVersion, f, a);
    this.authorizations = authorizations;
  }

  @Override
  protected Query newTermQuery(Term term) {
    return new AuthTermQuery(term, authorizations);
  }

  //TODO: Implement phrase queries, etc
}
