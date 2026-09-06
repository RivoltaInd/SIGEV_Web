<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
                <td>${cli.id}</td>
                <td>${cli.nombre}</td>
                <td>${cli.documento}</td>
                <td>${cli.telefono}</td>
                <td>${cli.email}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
