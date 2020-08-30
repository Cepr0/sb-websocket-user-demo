package io.github.cepr0.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages.anyMessage().hasRole("USER");
        // messages.nullDestMatcher().authenticated()
        //         .simpSubscribeDestMatchers("/user/queue/errors").permitAll()
        //         .simpDestMatchers("/app/**").hasRole("USER")
        //         .simpSubscribeDestMatchers("/user/**", "/topic/friends/*").hasRole("USER")
        //         // .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
        //         .anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}