package fts.bean;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageParser {
	private static Logger log = LoggerFactory.getLogger(PageParser.class);

	public static Set<String> getLinks(StringBuilder sb, String urlContext) {
		Set<String> result = new HashSet<String>();
		Pattern regex = Pattern.compile("<a\\s*href\\s*=\\s*(\\\"([^\"]*)\\\"|'[^']*'|([^'\">\\s]+))");
		Matcher regexMatcher = regex.matcher(sb);
		while (regexMatcher.find()) {
			if (3 != regexMatcher.groupCount()) {
				log.error("[PageParser] problem in grabbing link found group count " + regexMatcher.groupCount());
			}
			String normalisedUri = normaliseUri(urlContext, regexMatcher.group(2));

			if (null != normalisedUri) {
				result.add(normalisedUri);
			}
		}
		return result;
	}

	public static Page getPage(StringBuilder sb) {
		String pageContent = sb.toString().replaceAll("<style\\b[^>]*>([\\s\\S]*?)</style>", "").replaceAll("<script\\b[^>]*>([\\s\\S]*?)</script>", "").replaceAll("<[^>]*>", " ")
				.replaceAll("(\\s+)", " ").trim();
		return new Page("", getTitle(sb), pageContent);
	}

	private static String getTitle(StringBuilder sb) {
		Pattern regex = Pattern.compile("<title>([^<]*)</title>");
		Matcher regexMatcher = regex.matcher(sb);
		regexMatcher.find();
		return regexMatcher.group(1);
	}

	public static String normaliseUri(String context, String partURI) {
		if (null == partURI || partURI.contains("javascript:")) {
			return null;
		}

		URI uri;
		try {
			uri = new URI(context);

			return uri.resolve(partURI).toString();
		} catch (Exception e) {
			log.equals("[PageParser] error in " + partURI);
		}
		return null;

	}
}
