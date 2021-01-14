package org.galamat.speechtotextbot.telegrambot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galamat.speechtotextbot.util.FileDownloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

@Component
public class TelegramBotRunner {

    private final Logger logger = LogManager.getLogger(TelegramBotRunner.class.getName());

    @Autowired
    private AudioToTextTelegramLongPollingBot audioToTextTelegramLongPollingBot;

    public TelegramBotRunner() {
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(audioToTextTelegramLongPollingBot);
        } catch (TelegramApiException e) {
            logger.error(e);
        }
    }

}
