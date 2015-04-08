package org.digitalantiquity.hibsearch;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.lucene.queryparser.classic.ParseException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.EntityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example testcase for Hibernate Search
 */
public class IndexAndSearchTest {

    private EntityManagerFactory emf;
    private EntityManager em;
    
    private Session session;
    private SessionFactory sessionFactory;

    private static Logger log = LoggerFactory.getLogger(IndexAndSearchTest.class);

    @Before
    public void setUp() {
        initHibernate();
    }

    @After
    public void tearDown() {
        purge();
    }

    @Test
    public void testIndexAndSearch() throws Exception {
        List<Book> books = search("hibernate");
        assertEquals("Should get empty list since nothing is indexed yet", 0, books.size());

        index();

        // search by title
        books = search("hibernate");
        assertEquals("Should find one book", 1, books.size());
        assertEquals("Wrong title", "Java Persistence with Hibernate", books.get(0).getTitle());

        // search author
        books = search("\"Gavin King\"");
        assertEquals("Should find one book", 1, books.size());
        assertEquals("Wrong title", "Java Persistence with Hibernate", books.get(0).getTitle());
    }

    @Test
    public void testStemming() throws Exception {

        index();

        List<Book> books = search("refactor");
        assertEquals("Wrong title", "Refactoring: Improving the Design of Existing Code", books.get(0).getTitle());

        books = search("refactors");
        assertEquals("Wrong title", "Refactoring: Improving the Design of Existing Code", books.get(0).getTitle());

        books = search("refactored");
        assertEquals("Wrong title", "Refactoring: Improving the Design of Existing Code", books.get(0).getTitle());

        books = search("refactoring");
        assertEquals("Wrong title", "Refactoring: Improving the Design of Existing Code", books.get(0).getTitle());
    }

    private void initHibernate() {
        emf = Persistence.createEntityManagerFactory("hibernate-search-example");
        em = emf.createEntityManager();
        Configuration configuration = new Configuration();
        configuration.configure("hibernate_sp.cfg.xml");
        configuration.addAnnotatedClass(Book.class);
        configuration.addAnnotatedClass(Novel.class);
        configuration.addAnnotatedClass(Author.class);
        StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        sessionFactory = configuration.buildSessionFactory(ssrb.build());
        session = sessionFactory.openSession();

    }

    private void index() {
//        FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        FullTextSession ftEm = org.hibernate.search.Search.getFullTextSession(session);
        try {
            ftEm.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            log.error("Was interrupted during indexing", e);
        }
    }

    private void purge() {
        FullTextSession ftEm = org.hibernate.search.Search.getFullTextSession(session);
        ftEm.purgeAll(Book.class);
        ftEm.flushToIndexes();
        ftEm.close();
        emf.close();
    }

    private List<Book> search(String searchQuery) throws ParseException {
        org.hibernate.search.FullTextQuery query = searchQuery(searchQuery);

        List<Book> books = query.list();

        for (Book b : books) {
            log.info("Title: " + b.getTitle());
        }
        return books;
    }

    private org.hibernate.search.FullTextQuery searchQuery(String searchQuery) throws ParseException {
        FullTextSession fullTextEntityManager = org.hibernate.search.Search.getFullTextSession(session);

        EntityContext qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Book.class);

        String[] bookFields = { "title", "subtitle", "authors.name", "publicationDate", "subject" };
        org.apache.lucene.search.Query luceneQuery = qb.get().keyword().onFields(bookFields).matching(searchQuery).createQuery();

        final org.hibernate.search.FullTextQuery query = fullTextEntityManager.createFullTextQuery(luceneQuery, Book.class);

        return query;
    }
}
