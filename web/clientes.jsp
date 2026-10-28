<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Todo dato que viene de la base de datos se muestra con c:out, que lo escapa:
     si alguien guardo "<script>" se ve como texto y no se ejecuta (evita XSS). --%>
<!DOCTYPE html>
<html>
<head>
    <title>SIGEV - Clientes</title>
</head>
<body>
    <h2>Registrar Cliente</h2>
    <form action="ClienteServlet" method="post">
        Nombre: <input type="text" name="nombre" required maxlength="60"/><br/>
        Documento: <input type="text" name="documento" required pattern="[0-9]+" maxlength="15"/><br/>
        Telefono: <input type="text" name="telefono" pattern="[0-9]*" maxlength="15"/><br/>
        Email: <input type="email" name="email" maxlength="60"/><br/>
        <input type="submit" value="Guardar"/>
    </form>

    <h2>Clientes registrados</h2>
    <table border="1">
        <tr><th>ID</th><th>Nombre</th><th>Documento</th><th>Telefono</th><th>Email</th></tr>
        <c:forEach var="cli" items="${listaClientes}">
            <tr>
                <td><c:out value="${cli.id}"/></td>
                <td><c:out value="${cli.nombre}"/></td>
                <td><c:out value="${cli.documento}"/></td>
                <td><c:out value="${cli.telefono}"/></td>
                <td><c:out value="${cli.email}"/></td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
