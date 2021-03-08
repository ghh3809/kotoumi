package entity.service;

import kotlin.jvm.Transient;
import lombok.Data;

/**
 * @author guohaohao
 */
@Data
public class SifEvent {

    /**
     * 活动ID
     */
    private Integer eventId;
    /**
     * 活动名称
     */
    private String eventName;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 是否活跃
     */
    private Boolean active;

}
