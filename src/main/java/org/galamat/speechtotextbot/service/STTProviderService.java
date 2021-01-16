package org.galamat.speechtotextbot.service;

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
import org.galamat.speechtotextbot.util.FileDownloadUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;

@Service
public class STTProviderService {

    private final Logger logger = LogManager.getLogger(STTProviderService.class.getName());

    public SendMessage runOnVoiceTask(Update update, final String botToken) {
        logger.info(String.format("<<<<<<<<<<< Time start: %s", new Date(System.currentTimeMillis())));
        String messageText = onVoiceMessageAccepted(update, botToken);
        logger.info(String.format("Message received: %s", messageText));
        return constructTextOfVoiceMessage(messageText, String.valueOf(update.getMessage().getChatId()));
    }

    private String onVoiceMessageAccepted(Update update, String botToken) {
        logger.info("Accepted voice message");
        Voice voice = update.getMessage().getVoice();
        logger.info(String.format("Voice message duration: %s", voice.getDuration()));
        logger.info(String.format("Voice file id: %s", voice.getFileId()));

        final String badAudioMessage = "Не удется конвертировать аудио в текст";
        try {
            String filePath = getFilePath(botToken, voice.getFileId());
            String downloadVoiceUrl = String.format("https://api.telegram.org/file/bot%s/%s", botToken, filePath);
            String oggAudioPath = FileDownloadUtil.downloadFile(downloadVoiceUrl, filePath);

            if (oggAudioPath == null || oggAudioPath.isEmpty()) {
                return badAudioMessage;
            }
            long startTime = System.currentTimeMillis();
            String message = new STTProviderService().requestDsModel(oggAudioPath);
            logger.info(">>>>>>>>>>> Time Diff: " + (System.currentTimeMillis() - startTime));
            if (message.isEmpty()) {
                logger.warn(String.format("For audio: %s, received empty text", oggAudioPath));
                return badAudioMessage;
            }
            return message;
        } catch (ExecutionException | InterruptedException | IOException e ) {
            e.printStackTrace();
        }
        return badAudioMessage;
    }

    public String getFilePath(String botToken, String fileId) throws ExecutionException, InterruptedException {
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

    public String requestDsModel(String oggFilePath) throws IOException {
        File oggFile = new File(oggFilePath);
        HttpPost post = new HttpPost("http://192.168.88.224:8008/api/v1/stt");

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

    private SendMessage constructTextOfVoiceMessage(String messageText, String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        return message;
    }
}
