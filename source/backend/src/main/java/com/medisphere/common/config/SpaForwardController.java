package com.medisphere.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the bundled React app: client-side routes are forwarded to index.html. */
@Controller
public class SpaForwardController {

    @GetMapping({"/", "/login", "/admin", "/admin/*", "/doctor", "/doctor/*", "/nurse", "/nurse/*",
            "/reception", "/reception/*", "/pharmacy", "/pharmacy/inventory", "/lab", "/patient", "/patient/*"})
    public String forward() {
        return "forward:/index.html";
    }
}
