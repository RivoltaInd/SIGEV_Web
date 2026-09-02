<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>SIGEV - Registrar Producto</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f4f4; }
        .container { max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }
        h1 { color: #1E2B5E; }
        label { display: block; margin-top: 15px; font-weight: bold; color: #1E2B5E; }
        input, select { width: 100%; padding: 8px; margin-top: 5px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        button { margin-top: 20px; background-color: #1E2B5E; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; }
        button:hover { background-color: #6B21A8; }
        .enlace { margin-top: 20px; display: block; text-align: center; }
        a { color: #6B21A8; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Registrar Producto - SIGEV</h1>
        <form action="ProductoServlet" method="POST">
            <label for="nombre">Nombre</label>
            <input type="text" id="nombre" name="nombre" required>

            <label for="categoria">Categoría</label>
            <input type="text" id="categoria" name="categoria" required>

            <label for="precio">Precio</label>
            <input type="number" step="0.01" id="precio" name="precio" required>

            <label for="stock">Stock</label>
            <input type="number" id="stock" name="stock" required>

            <button type="submit">Registrar Producto</button>
        </form>
        <a class="enlace" href="ProductoServlet">Ver lista de productos →</a>
    </div>
</body>
</html>