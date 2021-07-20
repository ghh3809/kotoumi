package entity.response;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author guohaohao
 */
@Data
public class DialogueResponse {

    @JSONField(name = "error_code")
    private int errorCode;
    @JSONField(name = "error_msg")
    private String errorMsg;
    private Result result;

    @Data
    public static class Result {

        @JSONField(name = "session_id")
        private String sessionId;
        @JSONField(name = "dialog_state")
        private JSONObject dialogState;
        @JSONField(name = "response_list")
        private List<ResultResponse> responseList;

        @Data
        public static class ResultResponse {

            @JSONField(name = "action_list")
            private List<Action> actionList;

            @Data
            public static class Action {

                private String say;

            }

        }

    }

}
