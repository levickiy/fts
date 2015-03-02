package fts.service;


import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fts.bean.Crawler;
import fts.bean.Page;

@Service
public class CrawlerService {
	private Logger  log = LoggerFactory.getLogger(CrawlerService.class);
	@Value("${fts.crawler.scan_deep}")
	private Integer scanDeep;
	@Value("${fts.crawler.links_per_page}")
	private Integer linkCountLimit;
	
	private Crawler crawler = new Crawler();
	
	@Autowired
	private LuceneService luceneService;

	private String startUrl;

	public void start() {
		if(null == startUrl) {
			log.info("[CrawlerService] start url not set.");
			return;
		}


		crawler.init(scanDeep, linkCountLimit);
		crawler.setStartPage(startUrl);
		crawler.start();

		Set<Page> scannedPages = crawler.getResults();
		log.info("[CrawlerService] fetched " + scannedPages.size() + " page");
		Document doc;
		for(Page page : scannedPages) {
			doc = new Document();
			doc.add(new TextField("url", page.getUrl(), Store.YES));
			doc.add(new TextField("title", page.getTitle(), Store.YES));
			doc.add(new TextField("content", page.getContent(), Store.YES));
			try {
				luceneService.addDocument(doc);
			} catch (IOException e) {
				log.equals("[CrawlerService] problem with adding docoment to index " + e.getMessage());
			}
		}
	}

	public void stop() {
		crawler.stop();
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}
}
