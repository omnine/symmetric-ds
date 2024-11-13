package com.jumpmind.symmetric.console.ui;

import com.vaadin.flow.server.VaadinServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class AppServlet extends VaadinServlet {
    protected static final Logger logger = LoggerFactory.getLogger(AppServlet.class);

    protected void servletInitialized() throws ServletException {
        logger.info("Initializing the servlet");
        super.servletInitialized();
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Service request");
        MDC.put("engineName", "gui");
        response.addHeader("x-frame-options", "SAMEORIGIN");
        super.service(request, response);
    }
}
