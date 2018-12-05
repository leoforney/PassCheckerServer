package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletConfiguration;

import javax.servlet.annotation.WebServlet;


@WebServlet(urlPatterns = "/*", name = "PasscheckerserverServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = PasscheckerserverUI.class, productionMode = false)
public class PasscheckerserverServlet extends VaadinServlet { }