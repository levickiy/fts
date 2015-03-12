package fts.bean;

import java.util.List;

public class SearchResult {
	private List<Page> results;
	private Integer maxResultCount;

	public SearchResult(List<Page> results, Integer maxResultCount) {
		this.results = results;
		this.maxResultCount = maxResultCount;
	}

	public List<Page> getResults() {
		return results;
	}

	public Integer getMaxResultCount() {
		return maxResultCount;
	}
}
