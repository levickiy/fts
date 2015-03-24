package fts.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import fts.bean.SearchResult;
import fts.service.LuceneService;

@Controller
public class SearchController {
	@Autowired
	private LuceneService luceneService;
	
	private Logger  log = LoggerFactory.getLogger(SearchController.class);

	@RequestMapping("/search")
	public ModelAndView search(@RequestParam(value="q") String query) {
		Map<String, Object> model = new HashMap<String, Object>();

		SearchResult result = SearchResult.getEmpty(); 

		try {
			result = luceneService.newSearch(query);
		} catch (ParseException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		model.put("searchResult", result);
		model.put("query", query);
		
		return new ModelAndView("search", model);
	}
}
