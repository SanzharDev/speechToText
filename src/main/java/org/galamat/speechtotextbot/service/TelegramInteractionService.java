package org.galamat.speechtotextbot.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.CompletableFuture;


@Service
public class TelegramInteractionService {

    private final Logger logger = LogManager.getLogger(TelegramInteractionService.class.getName());

    private STTProviderService sttProviderService;

    public TelegramInteractionService(STTProviderService sttProviderService) {
        this.sttProviderService = sttProviderService;
    }

    public SendMessage runOnVoiceTask(Update update, final String botToken) {
        return sttProviderService.runOnVoiceTask(update, botToken);
    }

}
