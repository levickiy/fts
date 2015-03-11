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
	private boolean isDone = false;
	private Queue<Resource> queue = new ConcurrentLinkedQueue<Resource>();
	private Queue<Page> results = new ConcurrentLinkedQueue<Page>();
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
		Thread crawlerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				process();

			}
		});

		crawlerThread.start();
	}

	private void process() {
		Resource resource = new Resource(startPage, scanDeep);
		queue.add(resource);
		StringBuilder sb;

		while (true) {
			if (!isProcess) {
				log.info("Crawler stoped.");
				break;
			}
			log.info("Queue size: " + queue.size() + " loaded links count: " + loaded.size());

			resource = queue.poll();

			if (null == resource) {
				break;
			}

			if (loaded.contains(resource.url)) {
				log.info("Skipped " + resource.url);
				continue;
			}

			if (0 < resource.level) {
				try {
					sb = performHttpGet(resource.url);
					loaded.add(resource.url);
					log.info("Loaded " + resource.url + " level " + resource.level);

				} catch (IOException e) {
					log.error("Error during load url " + resource.url + " " + e.getMessage());
					continue;
				}

				Set<String> pageLinks = PageParser.getLinks(sb, resource.url);
				Page parsedPage = PageParser.getPage(sb);
				results.add(new Page(resource.url, parsedPage.getTitle(), parsedPage.getContent()));

				int counter = pageLinks.size();
				if (0 != linkCountLimit && pageLinks.size() > linkCountLimit) {
					counter = linkCountLimit;
				}

				if (1 < resource.level) {
					for (String link : pageLinks) {
						if (0 >= --counter) {
							break;
						}
						if (!loaded.contains(link)) {
							queue.add(new Resource(link, resource.level - 1));
						}
					}
				}
				log.info("Added " + (pageLinks.size() < linkCountLimit ? pageLinks.size() : linkCountLimit) + " new links");

			}
		}
		isDone = true;

		log.info("end.");
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

	public Queue<Page> getResults() {
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

	public boolean isDone() {
		return isDone;
	}
}