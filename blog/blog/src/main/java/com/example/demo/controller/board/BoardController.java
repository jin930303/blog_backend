package com.example.demo.controller.board;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
public class BoardController {

    @GetMapping(value = "/")
    public String boards(){

        return "board";
    }

    @GetMapping(value = "/createBoard")
    public String createBoard(){

        return "createBoard";
    }
}
