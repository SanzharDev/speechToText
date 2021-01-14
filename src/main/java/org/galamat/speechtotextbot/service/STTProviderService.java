package org.galamat.speechtotextbot.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class STTProviderService {

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

}
