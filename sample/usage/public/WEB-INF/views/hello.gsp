<html>
  <body>
    <% 3.times { %>
      Hello World!
    <% } %>
    <br>
    <% if (session != null) { %>
      My session id is ${session.id}
    <% } else println "No session created." %>
  </body>
</html>
