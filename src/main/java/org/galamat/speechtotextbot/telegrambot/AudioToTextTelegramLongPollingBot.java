package org.galamat.speechtotextbot.telegrambot;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.galamat.speechtotextbot.util.FileDownloadService;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AudioToTextTelegramLongPollingBot extends TelegramLongPollingBot {

    private final Logger logger = LogManager.getLogger(AudioToTextTelegramLongPollingBot.class.getName());

    private final String botToken;

    private final FileDownloadService fileDownloadService;

    public AudioToTextTelegramLongPollingBot(String botToken, FileDownloadService fileDownloadService) {
        this.botToken = botToken;
        this.fileDownloadService = fileDownloadService;
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
        String messageText = "Отправьте пожалуйста аудиосообщения на казахском языке";

        if (update.hasMessage() && update.getMessage().hasVoice()) {
            logger.info(String.format("<<<<<<<<<<< Time start: %s", new Date(System.currentTimeMillis())));
            messageText = onVoiceMessageAccepted(update);
            logger.info(String.format("Message received: %s", messageText));
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));
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
            String filePath = getFilePath(voice.getFileId());
            String downloadVoiceUrl = String.format("https://api.telegram.org/file/bot%s/%s", botToken, filePath);
            String oggAudioPath = fileDownloadService.downloadFile(downloadVoiceUrl, filePath);
            if (oggAudioPath == null || oggAudioPath.isEmpty()) {
                return "Cannot convert your voice message to text";
            }
            return requestDsModel(oggAudioPath);
        } catch (ExecutionException | InterruptedException | IOException e ) {
            e.printStackTrace();
        }
        return "Cannot convert your voice message to text";
    }

    private String requestDsModel(String oggFilePath) throws IOException {
        File oggFile = new File(oggFilePath);
        HttpPost post = new HttpPost("http://192.168.88.224:8000/api/v1/stt");

        FileBody fileBody = new FileBody(oggFile, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("speech", fileBody);
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(post);
        String responseMessage = EntityUtils.toString(response.getEntity());

        JSONObject jsonObject = new JSONObject(responseMessage);
        return String.valueOf(jsonObject.get("text"));
    }

    private String getFilePath(String fileId) throws ExecutionException, InterruptedException {
        HttpResponse response;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = String.format("https://api.telegram.org/bot%s/getFile?file_id=%s", botToken, fileId);
        HttpGet httpGet = new HttpGet(url);
        try {
            response = httpClient.execute(httpGet);
            logger.info(response);
            String JSONString = EntityUtils.toString(response.getEntity(),
                    "UTF-8");
            JSONObject jsonObject = new JSONObject(JSONString);
            return String.valueOf(jsonObject.getJSONObject("result").get("file_path"));
        } catch (IOException | JSONException e) {
            logger.warn(e);
        }
        return null;
    }

}
