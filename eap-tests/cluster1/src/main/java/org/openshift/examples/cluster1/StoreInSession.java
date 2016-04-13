package org.openshift.examples.cluster1;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class StoreInSession extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String KEY = "key";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    HttpSession session = request.getSession();
	    Object attribute = session.getAttribute(KEY);

	    PrintWriter out = response.getWriter();
	    out.print(String.valueOf(attribute));
	}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String value = request.getParameter(KEY);
        session.setAttribute(KEY, value);

        PrintWriter out = response.getWriter();
        out.print("OK");
    }
}
