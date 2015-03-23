package fts.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fts.bean.Page;
import fts.bean.SearchResult;

@Service
public class LuceneService {
	private Logger log = LoggerFactory.getLogger(LuceneService.class);
	private IndexWriter indexWriter;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;
	private QueryParser parser;

	@Value("${fts.lucene.index_path}")
	private String indexPath;

	private Directory index;

	@PostConstruct
	public void init() throws IOException {
		index = FSDirectory.open(Paths.get(indexPath));

		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		config.setRAMBufferSizeMB(256.0);

		indexWriter = new IndexWriter(index, config);

		indexReader = DirectoryReader.open(index);
		indexSearcher = new IndexSearcher(indexReader);

		parser = new QueryParser("content", analyzer);
	}

	@PreDestroy
	public void sthutdown() {
		try {
			indexReader.close();
		} catch (IOException e) {
			log.error("Problem with closing indexReader");
		}
		try {
			indexWriter.close();
		} catch (IOException e) {
			log.error("Problem with closing indexWriter");
		}
	}

	@Deprecated
	public void addDocuments(Iterable<Page> scannedPages) {
		try {
			for (Page page : scannedPages) {
				addDocument(page);
			}
			indexWriter.commit();

			log.info(indexWriter.numDocs() + " docs in index.");

		} catch (IOException e) {
			log.error("Problem with adding documents to index " + e.getMessage());
		}

	}

	public void addDocument(Page page) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("url", page.getUrl(), Store.YES));
		doc.add(new TextField("title", page.getTitle(), Store.YES));
		doc.add(new TextField("content", page.getContent(), Store.YES));

		indexWriter.addDocument(doc);
		indexWriter.commit();

	}

	public void clearIndex() {
		try {
			indexWriter.deleteAll();
			indexWriter.commit();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public SearchResult newSearch(String queryStr) throws ParseException, IOException {
		List<Page> results = new ArrayList<Page>();

		Query query = parser.parse(queryStr);
		TopDocs topDocs = indexSearcher.search(query, 10);
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; ++i) {
			Document d = indexSearcher.doc(hits[i].doc);
			results.add(new Page(d.get("url"), d.get("title"), d.get("content")));
		}

		return new SearchResult(results, topDocs.totalHits);
	}
}