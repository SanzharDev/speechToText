package org.galamat.speechtotextbot.util;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

public class FileDownloadUtil {

    private static final String DIRECTORY_NAME = "C:\\Users\\User\\IdeaProjects\\speach-to-text-bot\\src\\main\\resources";

    private static final Logger logger = LogManager.getLogger(FileDownloadUtil.class.getName());

    public static String downloadFile(String srcFileUrl, String dstFilePath) throws ExecutionException, InterruptedException, IOException {
        logger.info(String.format("Downloading file from: %s", srcFileUrl));
        String fileDestination = String.format("%s\\%s", DIRECTORY_NAME, dstFilePath);
        logger.info(String.format("Downloading file to: %s", fileDestination));
        try {
            URLConnection conn = new URL(srcFileUrl).openConnection();
            InputStream is = conn.getInputStream();
            File yourFile = new File(fileDestination);
            yourFile.createNewFile();
            OutputStream outStream = new FileOutputStream(yourFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            return fileDestination;
        } catch (IOException e) {
            logger.warn(e);
            logger.printf(Level.WARN, "File path: %s", fileDestination);
            return null;
        }
    }

}
