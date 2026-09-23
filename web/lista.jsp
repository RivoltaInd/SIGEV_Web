<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, com.sigev.modelo.Producto" %>
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Productos - SIGEV</title>
</head>
<body>
    <h2>Lista de Productos</h2>

    <% if (request.getAttribute("mensaje") != null) { %>
        <p><%= request.getAttribute("mensaje") %></p>
    <% } %>

    <table border="1" cellpadding="5">
        <tr>
            <th>Código</th>
            <th>Nombre</th>
            <th>Categoría</th>
            <th>Precio</th>
            <th>Stock</th>
        </tr>
        <%
            List<Producto> productos = (List<Producto>) request.getAttribute("productos");
            if (productos != null) {
                for (Producto p : productos) {
        %>
        <tr>
            <td><%= p.getCodigo() %></td>
            <td><%= p.getNombre() %></td>
            <td><%= p.getCategoria() %></td>
            <td>$<%= p.getPrecio() %></td>
            <td><%= p.getStock() %></td>
        </tr>
        <%
                }
            }
        %>
    </table>

    <br>
    <a href="index.jsp">Volver al formulario</a><br>
    <a href="formVenta.jsp">Ir a Ventas</a>
</body>
</html>