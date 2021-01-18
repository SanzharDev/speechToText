package org.galamat.speechtotextbot;

import org.galamat.speechtotextbot.telegrambot.TelegramLongPollingBotController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@SpringBootApplication
public class SpeechToTextBotApplication {

    @Value("${bot.token}")
    private String botToken;

    public static void main(String[] args) {
        SpringApplication.run(SpeechToTextBotApplication.class, args);
    }

    @Bean
    public TelegramLongPollingBotController telegramLongPollingBot() {
        return new TelegramLongPollingBotController(botToken, new DefaultBotOptions());
    }

}
