package org.galamat.speechtotextbot.telegrambot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galamat.speechtotextbot.service.TelegramInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TelegramLongPollingBotController extends TelegramLongPollingBot {

    private final Logger logger = LogManager.getLogger(TelegramLongPollingBotController.class.getName());

    private final String botToken;

    @Autowired
    TelegramInteractionService telegramInteractionService;

    public TelegramLongPollingBotController(String botToken) {
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
        long start = System.currentTimeMillis();
        logger.info("--------------------- Update received at {} ---------------------", new Date(start));
        if (update.hasMessage() && update.getMessage().hasVoice()) {
            logger.info("<<< Future started for chat {}.", update.getMessage().getChatId());
            CompletableFuture
                    .completedFuture(telegramInteractionService.runOnVoiceTask(update, botToken))
                    .thenApplyAsync(this::sendMessage);
            logger.info(">>> Future finished for chat {}. Total execution time: {}",
                    update.getMessage().getChatId(), System.currentTimeMillis() - start);
        }
    }

    public String sendMessage(SendMessage message) {
        try {
            execute(message);
            logger.info(String.format(">>>>>>>>>>> Time finish: %s", new Date(System.currentTimeMillis())));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return "Done";
    }
}
