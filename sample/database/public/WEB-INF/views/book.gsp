<html>
  <head>
    <title>Book List</title>
  </head>
<body>
    <h1>Book List</h1>
    <h2>
      <% for (book in request.getAttribute('books')) { %>
        id = ${book.id},
        title = ${book.title},
        author = ${book.author}
        <br>
      <% } %>
    </h2>
  </body>
</html>
