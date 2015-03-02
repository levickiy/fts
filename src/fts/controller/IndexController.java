package fts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import fts.service.CrawlerService;

@Controller
public class IndexController {
	@Value("${fts.lucene.index_path}")
	private String indexPath;
	
	@Autowired
	private CrawlerService crawlerService;
	@RequestMapping(value= "/index", method=RequestMethod.POST)
	public ModelAndView indexing(@RequestParam(value="q", required=false) String query) {

		crawlerService.setStartUrl(query);
		crawlerService.start();

		String message = "indexing page: " + query;
		return new ModelAndView("index", "message", message);
	}
	
	@RequestMapping(value= "/index", method=RequestMethod.GET)
	public ModelAndView index() {
		return new ModelAndView("index", "message", "Put page url and click button.");
	}
}
