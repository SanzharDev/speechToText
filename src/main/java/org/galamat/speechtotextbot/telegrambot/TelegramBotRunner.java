package org.galamat.speechtotextbot.telegrambot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

@Component
public class TelegramBotRunner {

    private final Logger logger = LogManager.getLogger(TelegramBotRunner.class.getName());

    private TelegramLongPollingBotController telegramLongPollingBotController;

    public TelegramBotRunner(TelegramLongPollingBotController telegramLongPollingBotController) {
        this.telegramLongPollingBotController = telegramLongPollingBotController;
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramLongPollingBotController);
        } catch (TelegramApiException e) {
            logger.error(e);
        }
    }

}
