package com.jumpmind.symmetric.console.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class RootController {
  @GetMapping({"/"})
  public RedirectView RootController(RedirectAttributes attributes) {
    return new RedirectView("/app/");
  }
}
