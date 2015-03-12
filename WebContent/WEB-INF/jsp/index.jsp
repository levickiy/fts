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

form div {
	margin-bottom: 5px;
}

input[name="q"] {
	width: 500px;
}
</style>
</head>
<body>
	<div class="content">
		<form action="./index" method="post">
			<div>
				<input type="text" name="q" value="${search}"></input> <input
					type="submit" value="Index"></input>
			</div>
			<div>
				<label for="scanDeep">Scan deep</label>
				<select id="scanDeep" name="d">
					<option>1</option>
					<option selected="selected">2</option>
					<option>3</option>
					<option>4</option>
				</select>
			</div>				
		</form>
		<div class="results">
			<div>${message}</div>
		</div>
		<div class="footer">
			<a href="./">/</a> | <a href="./index">index</a> | <a
				href="./indexclear">clear index</a>
		</div>
	</div>
</body>
</html>