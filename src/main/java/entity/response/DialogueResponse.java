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
        @JSONField(name = "responses")
        private List<ResultResponse> responses;

        @Data
        public static class ResultResponse {

            @JSONField(name = "actions")
            private List<Action> actions;

            @Data
            public static class Action {

                private String say;

            }

        }

    }

}
