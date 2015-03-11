package fts.service;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import fts.bean.Crawler;
import fts.bean.Page;

@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Service
public class CrawlerService {
	private Logger log = LoggerFactory.getLogger(CrawlerService.class);
	@Value("${fts.crawler.scan_deep}")
	private Integer scanDeep;
	@Value("${fts.crawler.links_per_page}")
	private Integer linkCountLimit;

	@Autowired
	private LuceneService luceneService;

	private String startUrl;

	public void start(String url) {
		this.startUrl = url;
		if (null == startUrl) {
			log.info("Start url not set.");
			return;
		}

		Thread crawlerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Crawler crawler = new Crawler();
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
}
