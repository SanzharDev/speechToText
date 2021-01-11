package org.galamat.speechtotextbot.telegrambot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galamat.speechtotextbot.util.FileDownloadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;

@Component
public class TelegramBotRunner {

    private final Logger logger = LogManager.getLogger(TelegramBotRunner.class.getName());

    private final FileDownloadService fileDownloadService;

    public TelegramBotRunner(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @Value("${bot.token}")
    private String botToken;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            AudioToTextTelegramLongPollingBot audioToTextBot = new AudioToTextTelegramLongPollingBot(botToken, fileDownloadService);
            botsApi.registerBot(audioToTextBot);
        } catch (TelegramApiException e) {
            logger.error(e);
        }
    }

}
