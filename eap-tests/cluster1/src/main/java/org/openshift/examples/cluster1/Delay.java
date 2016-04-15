package org.openshift.examples.cluster1;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Delay extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String d = request.getParameter("d");
        if (d == null || d.isEmpty())
            d = "180";
        int seconds;
        try {
            seconds = Integer.parseInt(d);
            if (seconds < 0)
                seconds = 0;
        } catch (NumberFormatException e) {
            seconds = 180;
        }

        out.println(String.format("Delaying %d seconds from node: %s", seconds, InetAddress.getLocalHost().toString()));
        out.flush();

        for (int i = 0; i < seconds; i++) {
            try {
                out.println("*");
                out.flush();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        out.println("Bye");
    }

}
