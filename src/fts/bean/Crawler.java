package fts.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Crawler {
	private int scanDeep;
	private int linkCountLimit;
	private String startPage;
	private boolean isProcess;
	private Queue<Resource> queue = new ConcurrentLinkedQueue<Resource>();
	private Set<Page> results = new HashSet<Page>();
	private Set<String> loaded = new HashSet<String>();

	private Logger log = LoggerFactory.getLogger(Crawler.class);
	
	public void init(int scanDeep, int linkCountLimit) {
		this.scanDeep = scanDeep;
		this.linkCountLimit = linkCountLimit;

	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;

	}

	public void stop() {
		isProcess = false;
	}
	
	public void start() {
		isProcess = true;
		Resource resource = new Resource(startPage, scanDeep);
		queue.add(resource);
		StringBuilder sb;
		int level = resource.level;

		while (true) {
			if(!isProcess) {
				log.info("[Crawler] Crawler stoped.");
				break;
			}
			log.info("[Crawler] queue size: " + queue.size() + " loaded links count: " + loaded.size());

			resource = queue.poll();

			if (null == resource) {
				break;
			}

			if (loaded.contains(resource.url)) {
				log.info("[Crawler] skipped link " + resource.url);
				continue;
			}

			try {
				log.info("[Crawler] loading link " + resource.url + " level " + resource.level);
				sb = performHttpGet(resource.url);
				
				
			} catch (IOException e) {
				log.info("[Crawler] error in loading url " + resource.url + "\n" + e.getMessage());
				continue;
			}
			level = resource.level - 1;

			if (0 <= level) {
				Set<String> pageLinks = PageParser.getLinks(sb, resource.url);
				Page parsedPage = PageParser.getPage(sb);
				results.add(new Page(resource.url, parsedPage.getTitle(), parsedPage.getContent()));

				int counter = pageLinks.size();
				if (0 != linkCountLimit && pageLinks.size() > linkCountLimit) {
					counter = linkCountLimit;
				}

				for (String link : pageLinks) {
					if (0 >= --counter) {
						break;
					}
					queue.add(new Resource(link, level));
				}
				log.info("[Crawler] added " + (pageLinks.size() < linkCountLimit ? pageLinks.size() : linkCountLimit) + " new links");
			}

			loaded.add(resource.url);
		}
		log.info("[Crawler] end");

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

	public Set<Page> getResults() {
		return results;

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