package tech.introduct.mailbox.socket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import tech.introduct.mailbox.socket.handler.MessageEventSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(messageSocketHandler(), "/websocket/messages/subscribe");
    }

    @Bean
    public MessageEventSocketHandler messageSocketHandler() {
        return new MessageEventSocketHandler();
    }

    @Bean
    public TaskScheduler serverTaskScheduler() {
        var taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(3);
        taskScheduler.setThreadNamePrefix("app-timer-");
        taskScheduler.setDaemon(true);
        return taskScheduler;
    }
}
