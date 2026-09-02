<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.sigev.modelo.Producto" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>SIGEV - Lista de Productos</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f4f4; }
        .container { max-width: 700px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        h1 { color: #1E2B5E; }
        .mensaje { padding: 10px; border-radius: 4px; margin-bottom: 15px; font-weight: bold; }
        .mensaje.ok { background-color: #d4edda; color: #155724; }
        .mensaje.error { background-color: #f8d7da; color: #721c24; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th { background-color: #1E2B5E; color: white; padding: 10px; text-align: left; }
        td { padding: 8px 10px; border-bottom: 1px solid #ddd; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        .enlace { margin-top: 20px; display: block; text-align: center; }
        a { color: #6B21A8; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Lista de Productos - SIGEV</h1>

        <%
            String mensaje = (String) request.getAttribute("mensaje");
            if (mensaje != null) {
                String claseCss = mensaje.startsWith("Producto registrado") ? "ok" : "error";
        %>
                <div class="mensaje <%= claseCss %>"><%= mensaje %></div>
        <%
            }
        %>

        <table>
            <tr>
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
                        <td><%= p.getNombre() %></td>
                        <td><%= p.getCategoria() %></td>
                        <td><%= p.getPrecio() %></td>
                        <td><%= p.getStock() %></td>
                    </tr>
            <%
                    }
                }
            %>
        </table>

        <a class="enlace" href="index.jsp">← Registrar otro producto</a>
    </div>
</body>
</html>