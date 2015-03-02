package fts.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fts.bean.Page;

@Service
public class LuceneService {
	@Value("${fts.lucene.index_path}")
	private String indexPath;
	
	private Directory index;

	public void initIndex() throws IOException {
		if(null == index) {
			//index =  new RAMDirectory();
			index = FSDirectory.open(Paths.get(indexPath));
		}
	}

	public void addDocument(Document doc) throws IOException {
		initIndex();
		
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(index, config);
		indexWriter.addDocument(doc);
		indexWriter.close();

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