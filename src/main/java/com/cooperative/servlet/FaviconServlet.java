package com.cooperative.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/favicon.ico")
public class FaviconServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Avoid noisy 404s in the browser console.
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
