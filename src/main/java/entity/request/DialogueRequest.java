package entity.request;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import constant.BotInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import utils.LoggerHelper;

@EqualsAndHashCode(callSuper = true)
@Data
public class DialogueRequest extends Request {

    private String version;
    @JSONField(name = "log_id")
    private String logId;
    @JSONField(name = "bot_id")
    private String botId;
    @JSONField(name = "service_id")
    private String serviceId;
    @JSONField(name = "bot_session")
    private String botSession;
    @JSONField(name = "session_id")
    private String sessionId;
    @JSONField(name = "dialog_state")
    private JSONObject dialogState;
    private Request request;

    private DialogueRequest() {

    }

    @Data
    public static class Request {
        @JSONField(name = "user_id")
        private String userId;
        private String query;
        @JSONField(name = "hyper_params")
        private JSONObject hyperParams;

        public Request(String userId, String query, boolean useProfile) {
            this.userId = userId;
            this.query = query;
            this.hyperParams = new JSONObject();
            if (useProfile) {
                hyperParams.put("chat_custom_bot_profile", 1);
                hyperParams.put("chat_default_bot_profile", 0);
            }
        }
    }

    /**
     * 获取一个示例
     * @param userId 用户ID
     * @param query 用户请求
     * @param sessionId sessionId
     * @return 对话请求
     */
    public static DialogueRequest getInstance(String userId, String query, String sessionId, JSONArray sysPresumedHist) {
        DialogueRequest dialogueRequest = new DialogueRequest();
        dialogueRequest.logId = LoggerHelper.getRandomID(10);
        dialogueRequest.version = "2.0";
        dialogueRequest.botId = BotInfo.UNIT_BOT_ID;
        dialogueRequest.serviceId = BotInfo.UNIT_SERVICE_ID;
        dialogueRequest.sessionId = StringUtils.isBlank(sessionId) ? "" : sessionId;
        dialogueRequest.dialogState = new JSONObject();
        JSONObject contexts = new JSONObject();
        contexts.put("SYS_REMEMBERED_SKILLS", BotInfo.UNIT_SKILL_IDS);
        contexts.put("SYS_PRESUMED_HIST", sysPresumedHist);
        dialogueRequest.dialogState.put("contexts", contexts);
        dialogueRequest.request = new Request(userId, query, false);
        return dialogueRequest;
    }

}
