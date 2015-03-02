<html>
<head>
<title>Full test search</title>
<style type="text/css">
.content {
	margin: auto;
	width: 300px;
}

form {
	margin-top: 100px;
}

input[name="q"] {
	width: 200px;
}
</style>
</head>
<body>
	<div class="content">
		<form action="./index" method="post">
			<input type="text" name="q" value="${search}"></input> <input type="submit" value="Index"></input>
		</form>
		<div class="results">
			<div>${message}</div>
		</div>
	</div>
</body>
</html>