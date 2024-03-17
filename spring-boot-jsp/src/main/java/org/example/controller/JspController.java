package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class JspController {

    @GetMapping("/jsp")
    public ModelAndView jsp() {
        return new ModelAndView("index");
    }
}
