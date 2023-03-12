package entity.request;

import com.alibaba.fastjson.annotation.JSONField;
import entity.service.GPTMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChatGPTRequest extends Request {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    private String model;
    private LinkedList<GPTMessage> messages;
    private double temperature;
    @JSONField(name = "max_tokens")
    private int maxTokens;

    /**
     * 获取一个示例
     * @param query 用户请求
     * @param messages 历史消息
     * @return 对话请求
     */
    public static ChatGPTRequest getInstance(String query, LinkedList<GPTMessage> messages) {
        ChatGPTRequest chatGPTRequest = new ChatGPTRequest();
        chatGPTRequest.model = "gpt-3.5-turbo";
        chatGPTRequest.maxTokens = 512;
        messages.add(new GPTMessage(ROLE_USER, query));
        if (messages.size() > 10) {
            messages.remove(0);
            messages.remove(0);
        }
        chatGPTRequest.messages = messages;
        chatGPTRequest.temperature = 1;
        return chatGPTRequest;
    }

}
