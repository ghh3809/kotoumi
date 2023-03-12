package processor;

import com.alibaba.fastjson.JSON;
import entity.request.ChatGPTRequest;
import entity.response.ChatGPTResponse;
import entity.service.GPTMessage;
import lombok.extern.slf4j.Slf4j;
import utils.FileHelper;
import utils.RequestHelper;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guohaohao
 */
@Slf4j
public class ChatGPTService {

    public static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final Map<String, SessionStatus> SESSION_STATUS_MAP = new ConcurrentHashMap<>();
    private static final int MAX_SESSION_TIME = 300000;

    private static final String CHATGPT_TOKEN;

    static {
        List<String> tokens = FileHelper.readLines(new File("chatgpt.token"));
        if (tokens != null && !tokens.isEmpty()) {
            CHATGPT_TOKEN = tokens.get(0);
        } else {
            CHATGPT_TOKEN = "";
        }
    }

    /**
     * 进行UNIT闲聊对话
     * @param userId 用户ID
     * @param query 用户query
     * @return 返回话术
     */
    public static String dialog(String userId, String query) {

        // 准备sessionId
        LinkedList<GPTMessage> messages = new LinkedList<>();
        if (SESSION_STATUS_MAP.containsKey(userId)) {
            SessionStatus sessionStatus = SESSION_STATUS_MAP.get(userId);
            if (System.currentTimeMillis() - sessionStatus.lastChatTime < MAX_SESSION_TIME) {
                messages = sessionStatus.messages;
            }
        }
        ChatGPTRequest chatGPTRequest = ChatGPTRequest.getInstance(query, messages);

        // 请求unit服务
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", String.format("Bearer %s", CHATGPT_TOKEN));
        int retry = 0;
        String response = null;
        while (retry < 3) {
            retry ++;
            response = RequestHelper.httpPost(OPENAI_URL, chatGPTRequest, headerMap);
            if (response != null) {
                break;
            }
        }

        if (response != null) {
            ChatGPTResponse chatGPTResponse = JSON.parseObject(response, ChatGPTResponse.class);
            if (chatGPTResponse.getChoices() != null && !chatGPTResponse.getChoices().isEmpty()) {
                GPTMessage message = chatGPTResponse.getChoices().get(0).getMessage();
                messages.add(message);
                SESSION_STATUS_MAP.put(userId, new SessionStatus(messages, System.currentTimeMillis()));
                return message.getContent().trim();
            }
        }
        log.error("Request unit service failed!");
        return null;
    }

    private static class SessionStatus {

        private final LinkedList<GPTMessage> messages;
        private final long lastChatTime;

        SessionStatus(LinkedList<GPTMessage> messages, long lastChatTime) {
            this.messages = messages;
            this.lastChatTime = lastChatTime;
        }
    }
}
