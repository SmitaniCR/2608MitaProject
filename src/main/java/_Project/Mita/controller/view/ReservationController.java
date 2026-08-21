package _Project.Mita.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reservations")
public class ReservationController {

    @GetMapping
    public String list() {
        return "admin/reservations/list";
    }
}
