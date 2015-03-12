<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<title>Full test search</title>
<style type="text/css">
.content {
	margin: auto;
	width: 600px;
}

.footer {
	text-align: center;
}

form {
	margin-top: 100px;
}

input[name="q"] {
	width: 500px;
}

.title {
	color: blue;
}

.url {
	color: green;
}
</style>
</head>
<body>
	<div class="content">
		<form action="./search">
			<input type="text" name="q" value="${query}"></input> <input
				type="submit" value="Search"></input>
		</form>
		<div>
		<c:if test="${0 == searchResult.maxResultCount}">
			No results for "${query}".
		</c:if>
		<c:if test="${0 != searchResult.maxResultCount}">
			Top ${fn:length(searchResult.results)} from ${searchResult.maxResultCount} results.
		</c:if>
		</div>
		<div class="results">
			<c:forEach var="item" items="${searchResult.results}">
				<div>
					<p>
						<b><a class="title" href="${item.url}">${item.title}</a></b><br/>
						<a class="url" href="${item.url}">${item.url}</a>
					</p>
				</div>
			</c:forEach>
			<div class="footer">
				<a href="./">/</a> | <a href="./index">index</a> | <a href="./indexclear">clear index</a>
			</div>
		</div>
	</div>
</body>
</html>