package com.cooperative.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No custom init required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String ctx = req.getContextPath();
        String uri = req.getRequestURI();

        boolean publicPath = uri.equals(ctx + "/login")
                || uri.equals(ctx + "/register")
                || uri.equals(ctx + "/logout")
                || uri.equals(ctx + "/favicon.ico")
                || uri.equals(ctx + "/places")
                || uri.startsWith(ctx + "/assets/")
                || uri.contains(".css")
                || uri.contains(".js")
                || uri.contains(".png")
                || uri.contains(".jpg")
                || uri.contains(".jpeg")
                || uri.contains(".gif")
                || uri.contains(".svg")
                || uri.contains(".ico")
                || uri.contains(".woff")
                || uri.contains(".woff2");

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("authUser") != null;

        if (loggedIn || publicPath) {
            chain.doFilter(request, response);
            return;
        }

        resp.sendRedirect(ctx + "/login");
    }

    @Override
    public void destroy() {
        // No resources to release.
    }
}
