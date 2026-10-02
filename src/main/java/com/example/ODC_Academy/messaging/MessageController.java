package com.example.ODC_Academy.messaging;

import com.example.ODC_Academy.messaging.MessageService.*;
import com.example.ODC_Academy.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final MessageService service;
    private final SecurityUtils security;

    public MessageController(MessageService s, SecurityUtils u) { this.service = s; this.security = u; }

    @GetMapping("/contacts") public List<Contact> contacts() { return service.contacts(security.getCurrentUser()); }
    @GetMapping("/inbox") public List<MessageDto> inbox() { return service.inbox(security.getCurrentUser()); }
    @GetMapping("/sent") public List<MessageDto> sent() { return service.sent(security.getCurrentUser()); }
    @GetMapping("/unread-count") public long unread() { return service.unread(security.getCurrentUser()); }
    @PatchMapping("/{id}/read") public void read(@PathVariable Long id) { service.markRead(id, security.getCurrentUser()); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(@Valid @RequestBody SendRequest r) { return service.send(r, security.getCurrentUser()); }
}
