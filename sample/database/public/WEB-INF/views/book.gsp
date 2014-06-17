<html>
  <head>
    <title>Book List</title>
  </head>
<body>
    <h1>Book List</h1>
    <h2>
      <table border=1 width=300>
        <tr align=center>
          <th>ID</th><th>Title</th><th>Author</th>
        </tr>
      <% for (book in request.getAttribute('books')) { %>
        <tr align=center>
          <td>${book.id}</td><td>${book.title}</td><td>${book.author}</td>
        </tr>
      <% } %>
      </table>
    </h2>
  </body>
</html>
