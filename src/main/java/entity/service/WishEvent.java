package entity.service;

/**
 * @author guohaohao
 */
public class WishEvent {

    /**
     * 祈愿池ID
     */
    private Integer id;
    /**
     * 祈愿池名称
     */
    private String wishEventName;
    /**
     * 五星范围
     */
    private String unitFiveRegion;
    /**
     * 四星范围
     */
    private String unitFourRegion;
    /**
     * 祈愿类型，1为90抽保底无up池，2为80抽保底武器up（75%）池，3为90抽保底角色up（50%）池且有大保底
     */
    private Integer wishType;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWishEventName() {
        return wishEventName;
    }

    public void setWishEventName(String wishEventName) {
        this.wishEventName = wishEventName;
    }

    public String getUnitFiveRegion() {
        return unitFiveRegion;
    }

    public void setUnitFiveRegion(String unitFiveRegion) {
        this.unitFiveRegion = unitFiveRegion;
    }

    public String getUnitFourRegion() {
        return unitFourRegion;
    }

    public void setUnitFourRegion(String unitFourRegion) {
        this.unitFourRegion = unitFourRegion;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getWishType() {
        return wishType;
    }

    public void setWishType(Integer wishType) {
        this.wishType = wishType;
    }
}
