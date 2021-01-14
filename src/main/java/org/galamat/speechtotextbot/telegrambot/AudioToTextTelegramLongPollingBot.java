package org.galamat.speechtotextbot.telegrambot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galamat.speechtotextbot.service.STTProviderService;
import org.galamat.speechtotextbot.service.TelegramInteractionService;
import org.galamat.speechtotextbot.util.FileDownloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AudioToTextTelegramLongPollingBot extends TelegramLongPollingBot {

    private final Logger logger = LogManager.getLogger(AudioToTextTelegramLongPollingBot.class.getName());

    private final String botToken;

    private final String badAudioMessage = "Не удется конвертировать аудио в текст";

    @Autowired
    TelegramInteractionService telegramInteractionService;

    public AudioToTextTelegramLongPollingBot(String botToken) {
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
        logger.info(String.format("--------------------- Update received at %s", new Date(System.currentTimeMillis())));
        if (update.hasMessage() && update.getMessage().hasVoice()) {
           Runnable task = () -> {
               logger.info(String.format("<<<<<<<<<<< Time start: %s", new Date(System.currentTimeMillis())));
               String messageText = onVoiceMessageAccepted(update);
               logger.info(String.format("Message received: %s", messageText));
               sendMessage(messageText, String.valueOf(update.getMessage().getChatId()));
           };
           Thread newThread = new Thread(task);
           newThread.start();
        }
    }

    private synchronized void sendMessage(String messageText, String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        try {
            execute(message);
            logger.info(String.format(">>>>>>>>>>> Time finish: %s", new Date(System.currentTimeMillis())));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String onVoiceMessageAccepted(Update update) {
        logger.info("Accepted voice message");
        Voice voice = update.getMessage().getVoice();
        logger.info(String.format("Voice message duration: %s", voice.getDuration()));
        logger.info(String.format("Voice file id: %s", voice.getFileId()));

        try {
            String filePath = telegramInteractionService.getFilePath(botToken, voice.getFileId());
            String downloadVoiceUrl = String.format("https://api.telegram.org/file/bot%s/%s", botToken, filePath);
            String oggAudioPath = FileDownloadUtil.downloadFile(downloadVoiceUrl, filePath);
            if (oggAudioPath == null || oggAudioPath.isEmpty()) {
                return "Не удется конвертировать аудио в текст";
            }
            String message = new STTProviderService().requestDsModel(oggAudioPath);
            if (message.isEmpty()) {
                logger.warn(String.format("For audio: %s, received empty text", oggAudioPath));
                return "Аудио записано в плохом качестве, пожалуйста повторите";
            }
            return message;
        } catch (ExecutionException | InterruptedException | IOException e ) {
            e.printStackTrace();
        }
        return "Не удется конвертировать аудио в текст";
    }


}
