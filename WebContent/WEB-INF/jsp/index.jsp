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
</style>
</head>
<body>
	<div class="content">
		<form action="./index" method="post">
			<input type="text" name="q" value="${search}"></input> <input
				type="submit" value="Index"></input>
		</form>
		<div class="results">
			<div>${message}</div>
		</div>
		<div class="footer">
			<a href="./">/</a> | <a href="./index">index</a>
		</div>
	</div>
</body>
</html>