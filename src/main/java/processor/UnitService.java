package processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import entity.request.DialogueRequest;
import entity.response.DialogueResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import utils.ConfigHelper;
import utils.RequestHelper;

import java.util.HashMap;

/**
 * @author guohaohao
 */
@Slf4j
public class UnitService {

    private static String accessToken = null;
    private static long accessTokeExpireTime = 0L;

    private static final HashMap<String, SessionStatus> SESSION_STATUS_MAP = new HashMap<>();
    private static final int MAX_SESSION_TIME = 300000;

    private static final boolean USE_NEW_CHAT = false;

    static {
        if (!USE_NEW_CHAT) {
            refreshAccessToken();
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
        String sessionId = null;
        if (SESSION_STATUS_MAP.containsKey(userId)) {
            SessionStatus sessionStatus = SESSION_STATUS_MAP.get(userId);
            if (System.currentTimeMillis() - sessionStatus.lastChatTime < MAX_SESSION_TIME) {
                sessionId = sessionStatus.sessionId;
            }
        }
        DialogueRequest dialogueRequest = DialogueRequest.getInstance(userId, query, sessionId);

        // 准备accessToken
        if (System.currentTimeMillis() >= accessTokeExpireTime) {
            refreshAccessToken();
        }

        // 请求unit服务
        String url = String.format("https://aip.baidubce.com/rpc/2.0/unit/service/v3/chat?access_token=%s", accessToken);
        String response = RequestHelper.httpPost(url, dialogueRequest);
        if (response != null) {
            DialogueResponse dialogueResponse = JSON.parseObject(response, DialogueResponse.class);
            if (dialogueResponse.getErrorCode() == 0) {
                sessionId = dialogueResponse.getResult().getSessionId();
                if (StringUtils.isNotBlank(sessionId)) {
                    SESSION_STATUS_MAP.put(userId, new SessionStatus(sessionId, System.currentTimeMillis()));
                }
                return dialogueResponse.getResult().getResponses().get(0).getActions().get(0).getSay();
            } else {
                log.error("Request unit service failed: error code {}", dialogueResponse.getErrorCode());
                return null;
            }
        } else {
            log.error("Request unit service failed!");
            return null;
        }
    }

    private static void refreshAccessToken() {
        String grantType = ConfigHelper.getProperties("grant_type");
        String clientId = ConfigHelper.getProperties("client_id");
        String clientSecret = ConfigHelper.getProperties("client_secret");
        String url = String.format("https://aip.baidubce.com/oauth/2.0/token?grant_type=%s&client_id=%s&client_secret=%s", grantType, clientId, clientSecret);
        String response = RequestHelper.httpGet(url);
        if (response != null) {
            JSONObject jsonObject = JSON.parseObject(response);
            String accessToken = jsonObject.getString("access_token");
            int expiresIn = jsonObject.getInteger("expires_in");
            UnitService.accessToken = accessToken;
            UnitService.accessTokeExpireTime = System.currentTimeMillis() + expiresIn * 1000;
        } else {
            log.error("Refresh access token failed!");
        }
    }

    private static class SessionStatus {

        String sessionId;
        long lastChatTime;

        SessionStatus(String sessionId, long lastChatTime) {
            this.sessionId = sessionId;
            this.lastChatTime = lastChatTime;
        }

    }

}
