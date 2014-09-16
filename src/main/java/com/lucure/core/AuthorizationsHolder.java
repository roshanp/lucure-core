package com.lucure.core;

import com.lucure.core.security.Authorizations;
import com.lucure.core.security.VisibilityEvaluator;

/**
 */
public class AuthorizationsHolder {
    public static final ThreadLocal<AuthorizationsHolder> threadAuthorizations =
      new ThreadLocal<AuthorizationsHolder>() {
          @Override protected AuthorizationsHolder initialValue() {
              return new AuthorizationsHolder(Authorizations.EMPTY);
          }
      };

    public static final AuthorizationsHolder EMPTY = new AuthorizationsHolder(
      Authorizations.EMPTY);

    private final Authorizations authorizations;
    private final VisibilityEvaluator visibilityEvaluator;

    public AuthorizationsHolder(Authorizations authorizations) {
        this.authorizations = authorizations;
        this.visibilityEvaluator = new VisibilityEvaluator(
          authorizations);
    }

    public Authorizations getAuthorizations() {
        return authorizations;
    }

    public VisibilityEvaluator getVisibilityEvaluator() {
        return visibilityEvaluator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorizationsHolder)) {
            return false;
        }

        AuthorizationsHolder that = (AuthorizationsHolder) o;

        if (authorizations != null ? !authorizations.equals(
          that.authorizations) : that.authorizations != null) {
            return false;
        }
        if (visibilityEvaluator != null ? !visibilityEvaluator.equals(
          that.visibilityEvaluator) : that.visibilityEvaluator != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = authorizations != null ? authorizations.hashCode() : 0;
        result = 31 * result +
                 (visibilityEvaluator != null ? visibilityEvaluator.hashCode() :
                  0);
        return result;
    }
}
