package entity.request;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import constant.BotInfo;
import utils.LoggerHelper;

public class DialogueRequest extends Request {

    @JSONField(name = "log_id")
    private String logId;
    @JSONField(name = "bot_id")
    private String botId;
    @JSONField(name = "bot_session")
    private String botSession;
    @JSONField(name = "session_id")
    private String version;
    private Request request;

    private DialogueRequest() {

    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getBotSession() {
        return botSession;
    }

    public void setBotSession(String botSession) {
        this.botSession = botSession;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

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

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public JSONObject getHyperParams() {
            return hyperParams;
        }

        public void setHyperParams(JSONObject hyperParams) {
            this.hyperParams = hyperParams;
        }
    }

    /**
     * 获取一个示例
     * @param userId 用户ID
     * @param query 用户请求
     * @param sessionId sessionId
     * @return 对话请求
     */
    public static DialogueRequest getInstance(String userId, String query, String sessionId) {
        DialogueRequest dialogueRequest = new DialogueRequest();
        dialogueRequest.logId = LoggerHelper.getRandomID(10);
        dialogueRequest.version = "2.0";
        dialogueRequest.botId = BotInfo.UNIT_BOT_ID;
        if (sessionId == null) {
            dialogueRequest.botSession = "";
        } else {
            dialogueRequest.botSession = String.format("{\"session_id\":\"%s\"}", sessionId);
        }
        dialogueRequest.request = new Request(userId, query, false);
        return dialogueRequest;
    }

}
