package org.knoxcraft.jetty.server;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.canarymod.logger.Logman;

public class LoginFilter extends DefaultFilter
{
    public static final String USER_SESSION="userSession";
    private static Logman logger=Logman.getLogman(LoginFilter.class.getName());
    
    public LoginFilter() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);
        UserSession userSession = session == null ? null 
                :  (UserSession) session.getAttribute(USER_SESSION);
        if (session == null || session.isNew() || userSession == null) {
            String login = String.format("%s/login", request.getContextPath());
            // TODO: if the request is "get", save the target for a later re-direct after authentication
            response.sendRedirect(login);
        }
        chain.doFilter(request, response);
    }

}
