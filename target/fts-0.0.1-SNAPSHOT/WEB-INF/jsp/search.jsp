<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>Full test search</title>
<style type="text/css">
.content {
	margin: auto;
	width: 300px;
}

.footer {
	text-align: center;
}

form {
	margin-top: 100px;
}

input[name="q"] {
	width: 200px;
}

h2 b {
	color: blue;
}

p a {
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
		<div>${search}</div>
		<div class="results">
			<c:forEach var="item" items="${results}">
				<div>
					<h2>
						<a href="${item.url}"><b>${item.title}</b></a>
					</h2>
					<p>
						<a href="${item.url}">${item.url}</a>
					</p>

				</div>
			</c:forEach>
			<div class="footer">
				<a href="./">/</a> | <a href="./index">index</a> | <a
					href="./search">search</a>
			</div>
		</div>
	</div>
</body>
</html>