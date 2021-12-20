package entity.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class NewDialogueResponse {

    @JSONField(name = "error_code")
    private int errorCode;
    @JSONField(name = "error_msg")
    private String errorMsg;
    private String result;

}
