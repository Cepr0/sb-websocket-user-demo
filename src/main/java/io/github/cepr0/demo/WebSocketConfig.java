package io.github.cepr0.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.util.Optional.ofNullable;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public WebSocketConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // @Override
    // public void configureMessageBroker(MessageBrokerRegistry config) {
    // 	config.enableSimpleBroker("/topic");
    // 	// config.setApplicationDestinationPrefixes("/app");
    // }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS().setSupressCors(true);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = ofNullable(accessor.getNativeHeader("X-Authorization"))
                            .map(val -> val.get(0))
                            .orElse(null);
                    String login = ofNullable(accessor.getNativeHeader("login"))
                            .map(val -> val.get(0))
                            .orElse(null);
                    log.info("[i] Headers - authorization: {}, login: {}", authorization, login);

                    if (authorization != null && login != null) {
                        String base64Credentials = authorization.substring("Basic".length()).trim();
                        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                        String[] values = credentials.split(":", 2);

                        String username = values[0];
                        String password = values[1];

                        if (!login.equals(username)) {
                            throw new BadCredentialsException("Username '" + username + "' doesn't match the login: " + login);
                        }

                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (passwordEncoder.matches(password, userDetails.getPassword())) {
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    login,
                                    null,
                                    userDetails.getAuthorities()
                            );
                            accessor.setUser(auth);
                        } else {
                            throw new BadCredentialsException("Bad credentials for user " + username);
                        }
                    } else {
                        throw new AuthenticationCredentialsNotFoundException("X-Authorization or login headers must not be empty!");
                    }
                }
                return message;
            }
        });
    }


}
