package fts.service;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fts.bean.Crawler;
import fts.bean.Page;

@Service
public class CrawlerService {
	private Logger log = LoggerFactory.getLogger(CrawlerService.class);
	@Value("${fts.crawler.scan_deep}")
	private Integer scanDeep;
	@Value("${fts.crawler.links_per_page}")
	private Integer linkCountLimit;

	private Crawler crawler = new Crawler();

	@Autowired
	private LuceneService luceneService;

	private String startUrl;

	public void start() {
		if (null == startUrl) {
			log.info("Start url not set.");
			return;
		}

		Thread crawlerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				crawler.init(scanDeep, linkCountLimit);
				crawler.setStartPage(startUrl);
				crawler.start();

				Set<Page> scannedPages = crawler.getResults();
				log.info("fetched " + scannedPages.size() + " page");
				luceneService.addDocuments(scannedPages);
			}
		});
		crawlerThread.start();
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
