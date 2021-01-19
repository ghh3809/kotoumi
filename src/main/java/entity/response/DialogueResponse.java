package entity.response;

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

        @JSONField(name = "bot_session")
        private String botSession;
        @JSONField(name = "response")
        private ResultResponse response;

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
