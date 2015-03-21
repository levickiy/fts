package fts.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fts.bean.Page;
import fts.bean.SearchResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/fts-servlet-test.xml" })
public class LuceneServiceTest {
	@Autowired
	private LuceneService service;

	@Test
	public void serviceTest() {
		assertNotNull(service);
	}

	@Test
	public void addDocumentsTest() throws ParseException, IOException {
		assertNotNull(service);

		List<Page> pageList = new ArrayList<Page>();
		for (int i = 0; i < 10; i++) {
			pageList.add(new Page("http://page" + i + ".com", "Title " + i, "Page " + i));
		}

		service.addDocuments(pageList);
		SearchResult searchResult = service.newSearch("Page");
		assertNotNull(searchResult);
		assertEquals(Integer.valueOf(10), searchResult.getMaxResultCount());
	}

	@Test
	public void newSearchTest() {
		assertNotNull(service);
		
		fail();
	}

	@Test
	public void clearIndexTest() {
		service.clearIndex();
		fail();
	}

}
