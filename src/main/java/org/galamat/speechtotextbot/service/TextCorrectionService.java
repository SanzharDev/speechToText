package org.galamat.speechtotextbot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TextCorrectionService {

    private void runOnButtonCallback(Update update) {
        String msg = update.getCallbackQuery().getData();
        Runnable task = null;
        if (msg.equals("Button 'yes' been pressed")) {
            task = () -> {
                SendMessage message = constructButtonReplyMessage("Спасибо!",
                        String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
//                sendMessage(message);
            };
        } else if (msg.equals("Button 'no' been pressed")) {
            task = () -> {
                SendMessage message = constructButtonReplyMessage("Введите исправленный текст",
                        String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
//                sendMessage(message);

            };
        }
        if (task != null) {
            Thread newThread = new Thread(task);
            newThread.start();
        }
    }

    private SendMessage constructButtonReplyMessage(String messageText, String charId) {
        SendMessage message = new SendMessage();
        message.setChatId(charId);
        message.setText(messageText);
        return message;
    }

    private void addCheckTextButtons(SendMessage sendMessage) {
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        InlineKeyboardButton noButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData("Button 'yes' been pressed");
        noButton.setText("Нет");
        noButton.setCallbackData("Button 'no' been pressed");
        List<InlineKeyboardButton> keyboardButtonsRow = Stream.of(yesButton, noButton).collect(Collectors.toList());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Stream.of(keyboardButtonsRow).collect(Collectors.toList()));
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

}
