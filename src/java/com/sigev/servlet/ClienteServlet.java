package com.sigev.servlet;

import com.sigev.dao.ClienteDAO;
import com.sigev.modelo.Cliente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Mismo patron que ProductoServlet (AA2-EV02):
 * doGet consulta y hace forward a clientes.jsp, doPost inserta y reutiliza doGet.
 */
@WebServlet("/ClienteServlet")
public class ClienteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ClienteDAO dao = new ClienteDAO();
        List<Cliente> lista = dao.consultarClientes();
        request.setAttribute("listaClientes", lista);
        request.getRequestDispatcher("clientes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String documento = request.getParameter("documento");
        String telefono = request.getParameter("telefono");
        String email = request.getParameter("email");

        // Validacion en el servidor, con las mismas reglas de ClienteApi: lo que
        // valida el formulario se puede saltar desde el navegador
        boolean valido = nombre != null && nombre.length() >= 3 && nombre.length() <= 100
                && nombre.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")
                && documento != null && documento.matches("\\d{6,10}")
                && (telefono == null || telefono.isBlank() || telefono.matches("\\d{7,10}"))
                && (email == null || email.isBlank() || com.sigev.api.ApiUtil.correoValido(email));
        if (valido) {
            Cliente cliente = new Cliente(nombre.trim(), documento, telefono, email);
            new ClienteDAO().insertarCliente(cliente);
        }

        doGet(request, response);
    }
}
