<%@ taglib uri="/WEB-INF/tlds/kct.tld" prefix="kct" %>
<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/fn.tld" prefix="fn" %>

<!DOCTYPE html>
<html><head>
<%-- <kct:headStuff title="Welcome to KnoxCraft Turtles"></kct:headStuff> --%>
<title>KnoxCraft Turtles</title>
</head>
<body>
<h1>Welcome to KnoxCraft Turtles!
</h1>
<p>
   Today's date: <%= (new java.util.Date()).toLocaleString() %>
</p>
<p>
<a href="kctupload.jsp"> Upload KnoxCraft Turtles code through the web</a>
</p>
</body> 
</html>