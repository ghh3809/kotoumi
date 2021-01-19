package utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guohaohao
 */
@Slf4j
public class FileHelper {

    private static final int REQUEST_TIMEOUT_MS = 300000;
    private static final CloseableHttpClient CLOSEABLE_HTTP_CLIENT = HttpClientBuilder.create().build();
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectionRequestTimeout(REQUEST_TIMEOUT_MS)
            .setSocketTimeout(REQUEST_TIMEOUT_MS)
            .setConnectTimeout(REQUEST_TIMEOUT_MS)
            .build();

    /**
     * 下载文件
     * @param fileUrl 文件URL
     * @param file 下载的文件示例
     * @return 创建好的文件
     */
    public static File download(String fileUrl, File file) {
        if (StringUtils.isBlank(fileUrl)) {
            log.error("Download file with blank url!");
            return null;
        }
        log.info("Download file: {}", fileUrl);
        try {
            HttpGet httpGet = new HttpGet(fileUrl);
            httpGet.setConfig(REQUEST_CONFIG);
            CloseableHttpResponse response = CLOSEABLE_HTTP_CLIENT.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                try (InputStream in = response.getEntity().getContent();
                     FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] b = new byte[1024];
                    int length;
                    while ((length = in.read(b)) > 0){
                        fos.write(b, 0, length);
                    }
                } catch (Exception e) {
                    log.error("Write file failed: {}", e.getMessage());
                    return null;
                }
                log.info("Successfully save file: {}", file.getPath());
                return file;
            } else {
                log.error("Download file failed: HTTP Status Code {}", code);
                return null;
            }
        } catch (IOException e) {
            log.error("Download file failed:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 将文件内容读取到变量中
     * @param file 文件
     */
    public static List<String> readLines(File file) {
        // 初始化
        List<String> lines = new ArrayList<>();
        BufferedReader br = null;

        try {

            Charset charset = StandardCharsets.UTF_8;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

            // 读文件
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            log.error("readLines error:{}", e.getMessage());
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("close file error:{}", e.getMessage());
                }
            }
        }

        return lines;
    }

    /**
     * 将文本内容写入文件
     * @param lines 文件行
     * @param file 文件
     * @param append 是否追加写入
     */
    public static void saveToFile(File file, List<String> lines, boolean append) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        Charset.defaultCharset();

        if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append),
                        StandardCharsets.UTF_8));
                for (String line : lines) {
                    bw.write(line + "\r\n");
                }
            } catch (IOException e) {
                log.error("saveToFile failed:{}", e.getMessage());
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        log.error("saveToFile error: {}", e.fillInStackTrace().toString());
                    }
                }
            }
        } else {
            log.error("saveToFile failed: file not exists or create failed");
        }

    }

}
