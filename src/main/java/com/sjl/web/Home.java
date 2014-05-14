package com.sjl.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class Home {

    @RequestMapping("/")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("name", "SJL");
        return "index";
    }

}
