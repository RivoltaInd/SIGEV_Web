<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>SIGEV - Registrar Venta</title>
</head>
<body>
    <c:if test="${not empty ventaRegistrada}">
        <c:choose>
            <c:when test="${ventaRegistrada}">
                <p style="color:green;">Venta registrada correctamente.</p>
            </c:when>
            <c:otherwise>
                <p style="color:red;">No se pudo registrar la venta (revisar stock disponible).</p>
            </c:otherwise>
        </c:choose>
    </c:if>

    <h2>Registrar Venta</h2>
    <form action="VentaServlet" method="post">
        Cliente:
        <select name="idCliente" required>
            <c:forEach var="cli" items="${listaClientes}">
                <option value="${cli.id}">${cli.nombre} - ${cli.documento}</option>
            </c:forEach>
        </select>
        <br/><br/>

        <table border="1">
            <tr><th>Incluir</th><th>Producto</th><th>Precio</th><th>Stock</th><th>Cantidad</th></tr>
            <c:forEach var="prod" items="${listaProductos}">
                <tr>
                    <td>
                        <input type="hidden" name="idProducto" value="${prod.codigo}"/>
                        <input type="hidden" name="precioUnitario" value="${prod.precio}"/>
                    </td>
                    <td>${prod.nombre}</td>
                    <td>${prod.precio}</td>
                    <td>${prod.stock}</td>
                    <td><input type="number" name="cantidad" min="0" max="${prod.stock}" value="0"/></td>
                </tr>
            </c:forEach>
        </table>
        <br/>
        <input type="submit" value="Registrar venta"/>
    </form>

    <h2>Historial de ventas</h2>
    <table border="1">
        <tr><th>ID</th><th>Fecha</th><th>Cliente</th><th>Producto</th><th>Cantidad</th><th>Total</th></tr>
        <c:forEach var="v" items="${listaVentas}">
            <tr>
                <td>${v.id}</td>
                <td>${v.fecha}</td>
                <td>${v.nombreCliente}</td>
                <td>${v.detalles[0].nombreProducto}</td>
                <td>${v.detalles[0].cantidad}</td>
                <td>${v.total}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>