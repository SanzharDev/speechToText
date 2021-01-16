package org.galamat.speechtotextbot.telegrambot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galamat.speechtotextbot.service.TelegramInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class TelegramLongPollingBotController extends TelegramLongPollingBot {

    private final Logger logger = LogManager.getLogger(TelegramLongPollingBotController.class.getName());

    private final String botToken;

    @Autowired
    TelegramInteractionService telegramInteractionService;

    public TelegramLongPollingBotController(String botToken, DefaultBotOptions defaultBotOptions) {
        super(defaultBotOptions);
        super.getOptions().setMaxThreads(16);
        this.botToken = botToken;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return "audioToText";
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("--------------------- Update for {} received at {} ---------------------", update.getMessage().getChatId(), new Date(System.currentTimeMillis()));
        if (update.hasMessage() && update.getMessage().hasVoice()) {
            CompletableFuture
                    .supplyAsync(() -> telegramInteractionService.runOnVoiceTask(update, botToken))
                    .thenApply(this::sendMessage);
        }
        logger.info("--------------------- Update call for {} finished at {}  ---------------------", update.getMessage().getChatId(), new Date(System.currentTimeMillis()));
    }

    public String sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return "DONE";
    }
}
