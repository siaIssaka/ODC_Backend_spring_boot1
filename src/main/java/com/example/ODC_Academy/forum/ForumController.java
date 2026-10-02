package com.example.ODC_Academy.forum;

import com.example.ODC_Academy.forum.ForumService.*;
import com.example.ODC_Academy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forums")
public class ForumController {
    private final ForumService service;
    private final SecurityUtils security;

    public ForumController(ForumService s, SecurityUtils u) { this.service = s; this.security = u; }

    @GetMapping("/formation/{id}/threads")
    public List<PostDto> threads(@PathVariable Long id) { return service.threads(id, security.getCurrentUser()); }

    @PostMapping("/formation/{id}/threads") @ResponseStatus(HttpStatus.CREATED)
    public PostDto create(@PathVariable Long id, @Valid @RequestBody ThreadRequest r) {
        return service.createThread(id, r, security.getCurrentUser());
    }

    @GetMapping("/threads/{id}")
    public ThreadView thread(@PathVariable Long id) { return service.thread(id, security.getCurrentUser()); }

    @PostMapping("/threads/{id}/replies") @ResponseStatus(HttpStatus.CREATED)
    public PostDto reply(@PathVariable Long id, @Valid @RequestBody ReplyRequest r) {
        return service.reply(id, r, security.getCurrentUser());
    }
}
