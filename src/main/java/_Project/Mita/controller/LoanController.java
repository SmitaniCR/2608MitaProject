package _Project.Mita.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/loans")
public class LoanController {

    @GetMapping
    public String list() {
        return "admin/loans/list";
    }
}
