package entity.response;

import com.alibaba.fastjson.annotation.JSONField;
import entity.service.GPTMessage;
import lombok.Data;

import java.util.List;

/**
 * @author guohaohao
 */
@Data
public class ChatGPTResponse {

    private String id;
    private String object;
    private int created;
    private String model;
    private Usage usage;
    private List<Choice> choices;

    @Data
    public static class Usage {
        @JSONField(name = "prompt_tokens")
        private int promptTokens;
        @JSONField(name = "completion_tokens")
        private int completionTokens;
        @JSONField(name = "total_tokens")
        private int totalTokens;
    }

    @Data
    public static class Choice {
        private GPTMessage message;
        @JSONField(name = "finish_reason")
        private String finishReason;
        private int index;
    }
}
