package fts.service;

import java.util.HashSet;
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

	@Autowired
	private LuceneService luceneService;

	public void start(final String url, final Integer deep) {
		if(null != deep) {
			this.scanDeep = deep;
		}
		Thread crawlerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Crawler crawler = new Crawler();
				crawler.init(scanDeep, linkCountLimit);
				crawler.setStartPage(url);
				crawler.start();

				int counter = 0;
				Set<Page> scannedPages;
				Page page;
				while (true) {
					if (crawler.isDone() && crawler.getResults().isEmpty()) {
						break;
					}

					if (!crawler.getResults().isEmpty()) {
						scannedPages = new HashSet<Page>();

						for (int i = 0; i < 10; i++) {
							page = crawler.getResults().poll();
							if (null == page) {
								break;
							}
							scannedPages.add(page);
							counter++;
						}

						luceneService.addDocuments(scannedPages);
						log.info("Added  " + scannedPages.size() + " page, " + counter);
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						crawler.stop();
						log.info("CrawlerService interrupted.");
						break;
					}
				}
				log.info("Total added " + counter + " page to lucene index.");

			}
		});
		crawlerThread.start();

	}
}
