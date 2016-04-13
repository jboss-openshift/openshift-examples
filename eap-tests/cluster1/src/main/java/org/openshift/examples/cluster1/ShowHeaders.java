package org.openshift.examples.cluster1;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShowHeaders extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
		    String headerName = e.nextElement();
		    out.println(String.format("%s: %s", headerName, request.getHeader(headerName)));
		}
		out.println();
		
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            String paramName = e.nextElement();
            out.println(String.format("%s: %s", paramName, request.getParameter(paramName)));
        }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    doGet(request, response);
	}
}
