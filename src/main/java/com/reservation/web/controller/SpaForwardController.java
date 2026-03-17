package com.reservation.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/programs",
            "/announcements",
            "/therapists",
            "/reviews",
            "/reservation/guest",
            "/reservation/search",
            "/reservation/member",
            "/reservation/my",
            "/mypage"
    })
    public String forwardToReactApp() {
        return "forward:/react-app/index.html";
    }
}
