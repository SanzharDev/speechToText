package org.galamat.speechtotextbot;

import org.galamat.speechtotextbot.telegrambot.AudioToTextTelegramLongPollingBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@SpringBootApplication
public class SpeechToTextBotApplication {

    @Value("${bot.token}")
    private String botToken;

    public static void main(String[] args) {
        SpringApplication.run(SpeechToTextBotApplication.class, args);
    }

    @Bean
    public AudioToTextTelegramLongPollingBot telegramLongPollingBot() {
        return new AudioToTextTelegramLongPollingBot(botToken);
    }

}
