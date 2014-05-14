package org.lucure.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
  public static final ColumnVisibility ADMINS_CV =
      new ColumnVisibility(ADMINS_GROUP);
  public static final ColumnVisibility ADMINS_EMPLOYEES_CV =
      new ColumnVisibility(ADMINS_GROUP + "|" + EMPLOYEES_GROUP);

  //fields
  public static final String NAME_FIELD = "name";
  public static final String ADDRESS_FIELD = "address";
  public static final String PHONE_FIELD = "phone";
  public static final String SSN_FIELD = "ssn";
  public static final String EMPLOYEEID_FIELD = "id";

  private static RAMDirectory ramDirectory;
  private static Analyzer analyzer;

  @BeforeClass
  public static void setup() throws Exception {
    //add a few restricted documents
    analyzer = new StandardAnalyzer(LUCENE_VERSION);
    IndexWriterConfig conf = new IndexWriterConfig(LUCENE_VERSION, analyzer);
    conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    ramDirectory = new RAMDirectory();

    try (IndexWriter indexWriter = new IndexWriter(ramDirectory, conf)) {
      indexWriter.addDocument(
          createEmployee("John Doe", "1234 Temporary Address",
              "Phone Number 1234", "SSN-1234", "ID-1234")
      );
      indexWriter.addDocument(
          createEmployee("Jane Doe", "4321 Temporary Address",
              "Phone Number 4321", "SSN-4321", "ID-4321")
      );
      indexWriter.addDocument(
          createEmployee("Bob Wolowitz", "1 International Space Station", "NA",
              "SSN-1", "ID-1")
      );
      indexWriter.addDocument(
          createEmployee("Big CEO", "1 Blvd", "NA", "SSN-2", "ID-2"));
      indexWriter.commit();
    }
  }

  static Document createEmployee(String name, String address, String phone,
                                 String ssn, String id) {
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
    document
        .add(new RestrictedField(EMPLOYEEID_FIELD, id, fieldType, ADMINS_CV));

    return document;
  }

  @AfterClass
  public static void tearDown() throws Exception {
    ramDirectory.close();
  }

  @Test
  public void testNoAuths_queryUnrestrictedField() throws Exception {

    try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
      IndexSearcher indexSearcher = new IndexSearcher(open);
      //Q: query for someone's name
      //A: Should return docs since name is not restrictured
      Term unrestrictedTerm = new Term(NAME_FIELD, "doe");
      Authorizations authorizations = new Authorizations();
      BooleanQuery booleanQuery = new BooleanQuery();
      booleanQuery.add(
          new BooleanClause(new AuthTermQuery(unrestrictedTerm, authorizations),
              BooleanClause.Occur.SHOULD)
      );
      TopDocs search = indexSearcher.search(booleanQuery, 10);
      ScoreDoc[] scoreDocs = search.scoreDocs;
      assertEquals(2, scoreDocs.length);

      //should only see the name field available though
      for (ScoreDoc scoreDoc : scoreDocs) {
        RestrictedStoredFieldVisitor restrictedStoredFieldVisitor =
            new RestrictedStoredFieldVisitor(authorizations);
        indexSearcher.doc(scoreDoc.doc, restrictedStoredFieldVisitor);
        Document document = restrictedStoredFieldVisitor.getDocument();
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
      booleanQuery.add(
          new BooleanClause(new AuthTermQuery(restrictedTerm, authorizations),
              BooleanClause.Occur.SHOULD)
      );
      TopDocs search = indexSearcher.search(booleanQuery, 10);
      ScoreDoc[] scoreDocs = search.scoreDocs;
      assertEquals(0, scoreDocs.length);
    }
  }

  @Test
  public void testNoAuths_queryMultipleFields() throws Exception {

    try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
      IndexSearcher indexSearcher = new IndexSearcher(open);
      //Q: query for someone's address and name
      //A: Since name is unrestricted, data should come back, but again no address field should be visible
      Term restrictedTerm = new Term(ADDRESS_FIELD, "address");
      Term unrestrictedTerm = new Term(NAME_FIELD, "doe");
      Authorizations authorizations = new Authorizations();
      BooleanQuery booleanQuery = new BooleanQuery();
      booleanQuery.add(
          new BooleanClause(new AuthTermQuery(restrictedTerm, authorizations),
              BooleanClause.Occur.SHOULD)
      );
      booleanQuery.add(
          new BooleanClause(new AuthTermQuery(unrestrictedTerm, authorizations),
              BooleanClause.Occur.SHOULD)
      );
      TopDocs search = indexSearcher.search(booleanQuery, 10);
      ScoreDoc[] scoreDocs = search.scoreDocs;
      assertEquals(2, scoreDocs.length);
      //should only see the name field available though
      for (ScoreDoc scoreDoc : scoreDocs) {
        RestrictedStoredFieldVisitor restrictedStoredFieldVisitor =
            new RestrictedStoredFieldVisitor(authorizations);
        indexSearcher.doc(scoreDoc.doc, restrictedStoredFieldVisitor);
        Document document = restrictedStoredFieldVisitor.getDocument();
        assertEquals(1, document.getFields().size());
        assertEquals(NAME_FIELD, document.getFields().get(0).name());
      }
    }
  }

  @Test
  public void testAuths_queryRestrictedField() throws Exception {

    try (DirectoryReader open = DirectoryReader.open(ramDirectory)) {
      IndexSearcher indexSearcher = new IndexSearcher(open);
      //Q: query for someone's address as employee
      //A: Since address is queryable as an employee, it should return
      Term restrictedTerm = new Term(ADDRESS_FIELD, "address");
      Authorizations authorizations = new Authorizations(EMPLOYEES_GROUP);
      BooleanQuery booleanQuery = new BooleanQuery();
      booleanQuery.add(
          new BooleanClause(new AuthTermQuery(restrictedTerm, authorizations),
              BooleanClause.Occur.SHOULD)
      );
      TopDocs search = indexSearcher.search(booleanQuery, 10);
      ScoreDoc[] scoreDocs = search.scoreDocs;
      assertEquals(2, scoreDocs.length);
      //should only see the name field available though
      for (ScoreDoc scoreDoc : scoreDocs) {
        RestrictedStoredFieldVisitor restrictedStoredFieldVisitor =
            new RestrictedStoredFieldVisitor(authorizations);
        indexSearcher.doc(scoreDoc.doc, restrictedStoredFieldVisitor);
        Document document = restrictedStoredFieldVisitor.getDocument();
        assertEquals(3, document.getFields().size());
        Set<String> names = new HashSet<>();
        for(IndexableField field : document) {
          names.add(field.name());
        }
        assertEquals(3, names.size());
        assertTrue(names.contains(NAME_FIELD));
        assertTrue(names.contains(ADDRESS_FIELD));
        assertTrue(names.contains(PHONE_FIELD));
      }
    }
  }
}
