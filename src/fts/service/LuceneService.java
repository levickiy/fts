package fts.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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

@Service
public class LuceneService {
	private Logger log = LoggerFactory.getLogger(LuceneService.class);

	@Value("${fts.lucene.index_path}")
	private String indexPath;

	private Directory index;

	public void initIndex() throws IOException {
		if (null == index) {
			index = FSDirectory.open(Paths.get(indexPath));
		}
	}

	public void addDocuments(Set<Page> scannedPages) {
		try {
			initIndex();

			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter indexWriter = new IndexWriter(index, config);

			Document doc;
			for (Page page : scannedPages) {
				doc = new Document();
				doc.add(new TextField("url", page.getUrl(), Store.YES));
				doc.add(new TextField("title", page.getTitle(), Store.YES));
				doc.add(new TextField("content", page.getContent(), Store.YES));

				indexWriter.addDocument(doc);
				indexWriter.commit();
			}

			indexWriter.close();

		} catch (IOException e) {
			log.error("Problem with adding documents to index " + e.getMessage());
		}

	}

	public void clearIndex() {
		try {
			initIndex();
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter indexWriter = new IndexWriter(index, config);

			indexWriter.deleteAll();
			indexWriter.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public List<Page> search(String queryStr) throws ParseException, IOException {
		initIndex();

		List<Page> results = new ArrayList<Page>();
		Analyzer analyzer = new StandardAnalyzer();
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher indexSearcher = new IndexSearcher(reader);

		QueryParser parser = new QueryParser("content", analyzer);
		Query query = parser.parse(queryStr);
		TopDocs topDocs = indexSearcher.search(query, 10);
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i < hits.length; ++i) {
			Document d = indexSearcher.doc(hits[i].doc);
			results.add(new Page(d.get("url"), d.get("title"), d.get("content")));
		}
		reader.close();
		return results;
	}
}