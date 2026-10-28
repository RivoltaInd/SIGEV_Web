<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Antes usaba scriptlets con <%= %>, que NO escapan el texto.
     Ahora usa JSTL con c:out, que lo escapa: si alguien guardo "<script>"
     como nombre de producto se ve como texto y no se ejecuta (evita XSS). --%>
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Productos - SIGEV</title>
</head>
<body>
    <h2>Lista de Productos</h2>

    <c:if test="${not empty mensaje}">
        <p><c:out value="${mensaje}"/></p>
    </c:if>

    <table border="1" cellpadding="5">
        <tr>
            <th>Código</th>
            <th>Nombre</th>
            <th>Categoría</th>
            <th>Precio</th>
            <th>Stock</th>
        </tr>
        <c:forEach var="p" items="${productos}">
            <tr>
                <td><c:out value="${p.codigo}"/></td>
                <td><c:out value="${p.nombre}"/></td>
                <td><c:out value="${p.categoria}"/></td>
                <td>$<c:out value="${p.precio}"/></td>
                <td><c:out value="${p.stock}"/></td>
            </tr>
        </c:forEach>
    </table>

    <br>
    <a href="index.jsp">Volver al formulario</a><br>
    <a href="VentaServlet">Ir a Ventas</a>
</body>
</html>
