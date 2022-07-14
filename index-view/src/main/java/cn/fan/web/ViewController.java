package cn.fan.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/main")
    public String view() throws Exception {
        return "view";
    }
    @GetMapping("/backstage")
    public String backstage() throws Exception {
        return "backstage";
    }
}
