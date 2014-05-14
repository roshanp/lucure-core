lucure-core
===========

Lucure provides Cell level security for Lucene. This means the user can only query and view fields that they are authorized to do so.

The current implementation will save the Column Visibility as a _payload_ in the Lucene index.

## TODO

* Remove Accumulo dependency
* Remove need to store the CV in the value of the stored field as well. Currently, we need to do this because the _RestrictedStoredFieldVisitor_ does not have access to the term payload.
* Open RestrictedField to serve not just strings, but numeric values as well
* Unit test
* Test with non-term queries. Implement Phrase queries

##Index

This section will provide a tutorial on how to index data with visibilities

	Document document = new Document();
    document.add(new Field(NAME_FIELD, name, fieldType));
    document.add(new RestrictedField(ADDRESS_FIELD, address, fieldType, ADMINS_EMPLOYEES_CV));
    
Use the _RestrictedField_ to attach the ColumnVisibility and save the _Document_

## Query
 
This section will provide a tutorial on how to query data with authorizations

	IndexSearcher indexSearcher = new IndexSearcher(open);
    Term restrictedTerm = new Term(ADDRESS_FIELD, "address");
    Authorizations authorizations = new Authorizations(EMPLOYEES_GROUP);
    BooleanQuery booleanQuery = new BooleanQuery();
    booleanQuery.add(
        new BooleanClause(new AuthTermQuery(restrictedTerm, authorizations), BooleanClause.Occur.SHOULD)
    );
    TopDocs search = indexSearcher.search(booleanQuery, 10);
    
Simply, just create an _AuthTermQuery_ with the correct _Authorizations_ and _Term_ and execute the query.

### Query Parser

Can use a Lucene typical Query Parser as well:

	Authorizations authorizations = new Authorizations(EMPLOYEES_GROUP);
    QueryParser parser = new AuthorizationsQueryParser(LUCENE_VERSION, ADDRESS_FIELD, analyzer, authorizations);
    Query query = parser.parse("Address");