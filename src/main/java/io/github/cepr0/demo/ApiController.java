package io.github.cepr0.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class ApiController {

    private final SimpMessagingTemplate template;
    private final SimpUserRegistry userRegistry;

    public ApiController(SimpMessagingTemplate template, SimpUserRegistry userRegistry) {
        this.template = template;
        this.userRegistry = userRegistry;
    }

    @PostMapping("/api/users/{username}/send")
    public ResponseEntity<?> sendMessage(@RequestBody Message message, @PathVariable String username) {
        Set<SimpUser> users = userRegistry.getUsers();
        if (users.stream().anyMatch(simpUser -> simpUser.getName().equals(username))) {
            template.convertAndSendToUser(username, "/messages", message);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
