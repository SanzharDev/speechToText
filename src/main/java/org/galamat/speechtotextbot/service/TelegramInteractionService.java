package org.galamat.speechtotextbot.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
public class TelegramInteractionService {

    private final Logger logger = LogManager.getLogger(TelegramInteractionService.class.getName());

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

}
