package fts.bean;

import java.util.Set;

import junit.framework.TestCase;
import fts.bean.Page;
import fts.bean.PageParser;

public class PageParserTest  extends TestCase {
	private StringBuilder sb;
	public void setUp() {
		sb = new StringBuilder();
		sb.append("<html><body>")
		.append("<title>page title</title>")
		.append("<a href=\"/foo/bar\" >test link</a>")
		.append("<a href=\"http:/foo.com/ss\" >test link too</a>")
		.append("</html></body>");
	}
	public void testGetPage() {
		Page page = PageParser.getPage("http://foo.bar", sb);
		assertEquals("page title test link test link too", page.getContent());
		assertEquals("http://foo.bar", page.getUrl());
	}

	public void testGetLinks() {
		Set<String> links = PageParser.getLinks(sb, "");
		assertEquals(2, links.size());
		assertEquals("/foo/bar", links.toArray()[0]);
		assertEquals("http:/foo.com/ss", links.toArray()[1]);
		
	}
	
	public void testGetTitle() {
		assertEquals("page title", PageParser.getPage("", sb).getTitle());
		
		sb = new StringBuilder();
		sb.append("<head style=\"overflow-y: scroll;\">	<title>Global Private Equity & Venture Capital Data | Technology | PitchBook</title>    <meta name=\"viewport\" content=\"width=device-width\" />");
		
		assertEquals("Global Private Equity & Venture Capital Data | Technology | PitchBook", PageParser.getPage("", sb).getTitle());
	}
	
	public void testNormaliseURI() {
		assertEquals("http://foo.bar/foo/bar.html", PageParser.normaliseUri("http://foo.bar", "/foo/bar.html"));
		assertEquals("http://bar.foo/index.html", PageParser.normaliseUri("http://foo.bar", "http://bar.foo/index.html"));
		assertNull(PageParser.normaliseUri("http://foo.bar", "javascript:init(0)"));
	}

}
