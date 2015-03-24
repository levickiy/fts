package fts.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fts.bean.Page;
import fts.bean.PageParser;

@Service
public class CrawlerService {
	private Logger log = LoggerFactory.getLogger(CrawlerService.class);
	@Value("${fts.crawler.scan_deep}")
	private Integer scanDeep;
	@Value("${fts.crawler.max_thread}")
	private Integer threadLimit;

	private ExecutorService execService;
	private Set<String> scannedLinks = Collections.synchronizedSet(new HashSet<String>());

	@Autowired
	private LuceneService luceneService;

	@PostConstruct
	public void init() {
		execService = new CustomThreadPoolExecutor(threadLimit);
	}

	@PreDestroy
	public void sthutdown() {
		execService.shutdown();
	}

	public void start(final String url, final Integer deep) {
		if (null != deep) {
			this.scanDeep = deep;
		}
		execService.execute(new CrawlerThread(new Resource(url, deep)));
	}

	private class CrawlerThread implements Runnable {
		private Resource resource;
		private StringBuilder sb;

		public CrawlerThread(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void run() {
			try {

				sb = performHttpGet(resource.url);

				Set<String> pageLinks = PageParser.getLinks(sb, resource.url);
				log.info("Loaded: " + resource.url + " level: " + resource.level);

				Page parsedPage = PageParser.getPage(resource.url, sb);
				boolean isNew;
				if (1 < resource.level) {
					for (String url : pageLinks) {
						isNew = true;

						synchronized (scannedLinks) {
							if (!scannedLinks.contains(url)) {
								scannedLinks.add(url);
								isNew = false;
							} 
						}
						if(isNew) {
							execService.execute(new CrawlerThread(new Resource(url, resource.level - 1)));
						}
					}

				}
				luceneService.addDocument(parsedPage);

			} catch (IOException e) {
				log.error("Has error, resource with: " + resource.url + " level: " + resource.level);
			}

		}

		public StringBuilder performHttpGet(String url) throws IOException {
			StringBuilder stringBuilder = new StringBuilder();
			String line;

			URLConnection urlConnection = (new URL(url)).openConnection();

			urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");

			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}

			reader.close();

			return stringBuilder;

		}

	}

	private class CustomThreadPoolExecutor extends ThreadPoolExecutor {
		@Override
		protected void afterExecute(Runnable paramRunnable, Throwable paramThrowable) {
			long leftCount = getTaskCount() - getCompletedTaskCount();
			log.info("active: " + getActiveCount() + " task: " + getTaskCount() + " complete: " + getCompletedTaskCount() + " left: " + leftCount);

			if (1 >= leftCount) {
				scannedLinks.clear();
				log.info("total loaded:" + getCompletedTaskCount());
			}
		}

		public CustomThreadPoolExecutor(int paramInt) {
			super(paramInt, paramInt, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
		}

	}

	public class Resource {
		public String url;
		public int level;

		public Resource(String url, int level) {
			this.url = url;
			this.level = level;
		}
	}

}
