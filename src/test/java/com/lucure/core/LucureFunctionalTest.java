package com.lucure.core;

import com.lucure.core.codec.LucureCodec;
import com.lucure.core.index.LucureIndexSearcher;
import com.lucure.core.query.AuthorizationsQueryParser;
import com.lucure.core.security.Authorizations;
import com.lucure.core.security.ColumnVisibility;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(JUnit4.class)
public class LucureFunctionalTest {

    public static final Version LUCENE_VERSION = Version.LUCENE_47;

    //auths
    public static final String ADMINS_GROUP = "admins";
    public static final String EMPLOYEES_GROUP = "employees";

    //cvs
    public static final ColumnVisibility ADMINS_CV = new ColumnVisibility(
      ADMINS_GROUP);
    public static final ColumnVisibility ADMINS_EMPLOYEES_CV =
      new ColumnVisibility(ADMINS_GROUP + "|" + EMPLOYEES_GROUP);

    //fields
    public static final String NAME_FIELD = "name";
    public static final String ADDRESS_FIELD = "address";
    public static final String PHONE_FIELD = "phone";
    public static final String SSN_FIELD = "ssn";
    public static final String EMPLOYEEID_FIELD = "id";
    public static final String AGE_FIELD = "age";
    public static final String DESCRIPTION_FIELD = "description";

    private static RAMDirectory ramDirectory;
    private static Analyzer analyzer;

    @BeforeClass
    public static void setup() throws Exception {
        //add a few restricted documents
        analyzer = new StandardAnalyzer(LUCENE_VERSION);
        IndexWriterConfig conf = new IndexWriterConfig(LUCENE_VERSION,
                                                       analyzer);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        conf.setCodec(new LucureCodec());
        ramDirectory = new RAMDirectory();

        try (IndexWriter indexWriter = new IndexWriter(ramDirectory, conf)) {
            indexWriter.addDocument(createEmployee("John Doe",
                                                   "1234 Temporary Address",
                                                   "Phone Number 1234",
                                                   "SSN-1234", "ID-1234", 23,
                                                   "A model employee", true));
            indexWriter.addDocument(createEmployee("Jane Doe",
                                                   "4321 Temporary Address",
                                                   "Phone Number 4321",
                                                   "SSN-4321", "ID-4321", 35,
                                                   "Married to John Doe",
                                                   true));
            indexWriter.addDocument(createEmployee("Bob Wolowitz",
                                                   "1 International Space " +
                                                   "Station",
                                                   "NA", "SSN-1", "ID-1", 30,
                                                   "A model employee", false));
            indexWriter.addDocument(createEmployee("Big CEO", "1 Blvd", "NA",
                                                   "SSN-2", "ID-2", 45,
                                                   "A model employee", false));
            indexWriter.commit();
        }
    }

    static Document createEmployee(
      String name, String address, String phone, String ssn, String id, int age,
      String description, boolean descriptionRestricted) {
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(true);
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.freeze();

        Document document = new Document();
        document.add(new Field(NAME_FIELD, name, fieldType));
        document.add(new RestrictedField(ADDRESS_FIELD, address, fieldType,
                                         ADMINS_EMPLOYEES_CV));
        document.add(new RestrictedField(PHONE_FIELD, phone, fieldType,
                                         ADMINS_EMPLOYEES_CV));
        document.add(new RestrictedField(SSN_FIELD, ssn, fieldType, ADMINS_CV));
        document.add(new RestrictedField(EMPLOYEEID_FIELD, id, fieldType,
                                         ADMINS_CV));
        document.add(new RestrictedField(AGE_FIELD, age, fieldType, ADMINS_CV));
        document.add(new RestrictedField(DESCRIPTION_FIELD, description,
                                         fieldType,
                                         descriptionRestricted ? ADMINS_CV :
                                         ADMINS_EMPLOYEES_CV));

        return document;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ramDirectory.close();
    }

    @Test
    public void testNoAuths_queryUnrestrictedField() throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            LucureIndexSearcher indexSearcher = new LucureIndexSearcher(open);
            //Q: query for someone's name
            //A: Should return docs since name is not restrictured
            Term unrestrictedTerm = new Term(NAME_FIELD, "doe");
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new BooleanClause(new TermQuery(unrestrictedTerm),
                                               BooleanClause.Occur.SHOULD));
            TopDocs search = indexSearcher.search(booleanQuery, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(2, scoreDocs.length);

            //should only see the name field available though
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc,
                                                      new Authorizations());
                assertEquals(1, document.getFields().size());
                assertEquals(NAME_FIELD, document.getFields().get(0).name());
            }
        }
    }

    @Test
    public void testNoAuths_queryUnrestrictedFieldMatchAll() throws Exception {
        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            LucureIndexSearcher indexSearcher = new LucureIndexSearcher(open);
            //Q: query for someone's name
            //A: Should return docs since name is not restrictured
            Authorizations authorizations = new Authorizations();
            TopDocs search = indexSearcher.search(new MatchAllDocsQuery(), 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(4, scoreDocs.length);

            //should only see the name field available though
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc,
                                                      authorizations);
                assertEquals(1, document.getFields().size());
                assertEquals(NAME_FIELD, document.getFields().get(0).name());
            }
        }
    }

    @Test
    public void testNoAuths_queryRestrictedField() throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            IndexSearcher indexSearcher = new IndexSearcher(open);
            //Q: query for someone's address
            //A: Since address is a restricted field, nothing should return
            Term restrictedTerm = new Term(ADDRESS_FIELD, "address");
            Authorizations authorizations = new Authorizations();
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new BooleanClause(new TermQuery(restrictedTerm),
                                               BooleanClause.Occur.SHOULD));
            AuthorizationsHolder.threadAuthorizations.set(
              new AuthorizationsHolder(authorizations));
            TopDocs search = indexSearcher.search(booleanQuery, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(0, scoreDocs.length);
        }
    }

    /**
     * Tests if a non admin user queries for a term that exists in 3
     * documents, but only viewable in 2 through the specified auth
     *
     * @throws Exception
     */
    @Test
    public void test_queryTermInMultipleFields() throws Exception {
        assertFoundDocuments(2, new Authorizations(EMPLOYEES_GROUP));
    }

    @Test
    public void test_queryTermInMultipleFieldsAsAdmin() throws Exception {
        assertFoundDocuments(3, new Authorizations(ADMINS_GROUP));
    }

    private void assertFoundDocuments(
      int expected, Authorizations authorizations) throws IOException {
        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            IndexSearcher indexSearcher = new IndexSearcher(open);
            Term descriptionTerm = new Term(DESCRIPTION_FIELD, "model");
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new BooleanClause(new TermQuery(descriptionTerm),
                                               BooleanClause.Occur.SHOULD));
            AuthorizationsHolder.threadAuthorizations.set(
              new AuthorizationsHolder(authorizations));
            TopDocs search = indexSearcher.search(booleanQuery, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(expected, scoreDocs.length);
        }
    }

    @Test
    public void testNoAuths_queryMultipleFields() throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            LucureIndexSearcher indexSearcher = new LucureIndexSearcher(open);
            //Q: query for someone's address and name
            //A: Since name is unrestricted, data should come back,
            // but again no address field should be visible
            Term restrictedTerm = new Term(ADDRESS_FIELD, "address");
            Term unrestrictedTerm = new Term(NAME_FIELD, "doe");
            Authorizations authorizations = new Authorizations();
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new BooleanClause(new TermQuery(restrictedTerm),
                                               BooleanClause.Occur.SHOULD));
            booleanQuery.add(new BooleanClause(new TermQuery(unrestrictedTerm),
                                               BooleanClause.Occur.SHOULD));
            AuthorizationsHolder.threadAuthorizations.set(
              new AuthorizationsHolder(authorizations));
            TopDocs search = indexSearcher.search(booleanQuery, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(2, scoreDocs.length);
            //should only see the name field available though
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc,
                                                      authorizations);
                assertEquals(1, document.getFields().size());
                assertEquals(NAME_FIELD, document.getFields().get(0).name());
            }
        }
    }

    @Test
    public void testNoAuths_queryTopDocsCollector() throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            LucureIndexSearcher indexSearcher = new LucureIndexSearcher(open);
            //Q: query for someone's address and name
            //A: Since name is unrestricted, data should come back,
            // but again no address field should be visible
            Term restrictedTerm = new Term(ADDRESS_FIELD, "address");
            Term unrestrictedTerm = new Term(NAME_FIELD, "doe");
            Authorizations authorizations = new Authorizations();
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new BooleanClause(new TermQuery(restrictedTerm),
                                               BooleanClause.Occur.SHOULD));
            booleanQuery.add(new BooleanClause(new TermQuery(unrestrictedTerm),
                                               BooleanClause.Occur.SHOULD));
            AuthorizationsHolder.threadAuthorizations.set(
              new AuthorizationsHolder(authorizations));
            TopScoreDocCollector topScoreDocCollector =
              TopScoreDocCollector.create(10, true);
            indexSearcher.search(booleanQuery, topScoreDocCollector);
            TopDocs search = topScoreDocCollector.topDocs();
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(2, scoreDocs.length);
            //should only see the name field available though
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc,
                                                      authorizations);
                assertEquals(1, document.getFields().size());
                assertEquals(NAME_FIELD, document.getFields().get(0).name());
            }
        }
    }

    @Test
    public void testAuths_queryRestrictedField() throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            LucureIndexSearcher indexSearcher = new LucureIndexSearcher(open);
            //Q: query for someone's address as employee
            //A: Since address is queryable as an employee, it should return
            Term restrictedTerm = new Term(ADDRESS_FIELD, "address");
            Authorizations authorizations = new Authorizations(EMPLOYEES_GROUP);
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new BooleanClause(new TermQuery(restrictedTerm),
                                               BooleanClause.Occur.SHOULD));
            TopDocs search = indexSearcher.search(booleanQuery, 10, authorizations);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(2, scoreDocs.length);
            //should only see the name field available though
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc,
                                                      authorizations);
                assertEquals(3, document.getFields().size());
                Set<String> names = new HashSet<>();
                for (IndexableField field : document) {
                    names.add(field.name());
                }
                assertEquals(3, names.size());
                assertTrue(names.contains(NAME_FIELD));
                assertTrue(names.contains(ADDRESS_FIELD));
                assertTrue(names.contains(PHONE_FIELD));
            }
        }
    }

    @Test
    public void testAuths_queryRestrictedFieldWithParser() throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            LucureIndexSearcher indexSearcher = new LucureIndexSearcher(open);
            //Q: query for someone's address as employee
            //A: Since address is queryable as an employee, it should return
            Authorizations authorizations = new Authorizations(EMPLOYEES_GROUP);
            QueryParser parser = new AuthorizationsQueryParser(LUCENE_VERSION,
                                                               ADDRESS_FIELD,
                                                               analyzer,
                                                               authorizations);
            Query query = parser.parse("Address");

            TopDocs search = indexSearcher.search(query, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(2, scoreDocs.length);
            //should only see the name field available though
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc,
                                                      authorizations);
                assertEquals(3, document.getFields().size());
                Set<String> names = new HashSet<>();
                for (IndexableField field : document) {
                    names.add(field.name());
                }
                assertEquals(3, names.size());
                assertTrue(names.contains(NAME_FIELD));
                assertTrue(names.contains(ADDRESS_FIELD));
                assertTrue(names.contains(PHONE_FIELD));
            }
        }
    }

    @Test
    public void testAuths_queryRestrictedFieldWithParserAdmin()
      throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            LucureIndexSearcher indexSearcher = new LucureIndexSearcher(open);
            //Q: query for someone's address as employee
            //A: Since address is queryable as an employee, it should return
            Authorizations authorizations = new Authorizations(ADMINS_GROUP);
            QueryParser parser = new AuthorizationsQueryParser(LUCENE_VERSION,
                                                               ADDRESS_FIELD,
                                                               analyzer,
                                                               authorizations);
            Query query = parser.parse("Address");

            TopDocs search = indexSearcher.search(query, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(2, scoreDocs.length);
            //should only see the name field available though
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc,
                                                      authorizations);
                assertEquals(7, document.getFields().size());
                Set<String> names = new HashSet<>();
                for (IndexableField field : document) {
                    names.add(field.name());
                }
                assertEquals(7, names.size());
                assertTrue(names.contains(NAME_FIELD));
                assertTrue(names.contains(ADDRESS_FIELD));
                assertTrue(names.contains(PHONE_FIELD));
                assertTrue(names.contains(AGE_FIELD));
                assertTrue(names.contains(SSN_FIELD));
                assertTrue(names.contains(EMPLOYEEID_FIELD));
                assertTrue(names.contains(DESCRIPTION_FIELD));
            }
        }
    }

    @Test
    public void testNoAuths_queryRestrictedFieldWithParser() throws Exception {

        try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
            IndexSearcher indexSearcher = new IndexSearcher(open);
            //Q: query for someone's address
            //A: Since address is a restricted field, nothing should return
            Authorizations authorizations = new Authorizations();
            QueryParser parser = new AuthorizationsQueryParser(LUCENE_VERSION,
                                                               ADDRESS_FIELD,
                                                               analyzer,
                                                               authorizations);
            Query query = parser.parse("\"Temporary Address\"");
            TopDocs search = indexSearcher.search(query, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            assertEquals(0, scoreDocs.length);
        }
    }
}
