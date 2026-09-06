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

        Cliente cliente = new Cliente(nombre, documento, telefono, email);
        new ClienteDAO().insertarCliente(cliente);

        doGet(request, response);
    }
}
