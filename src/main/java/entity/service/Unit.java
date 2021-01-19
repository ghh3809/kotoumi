package entity.service;

/**
 * @author guohaohao
 */
public class Unit {

    /**
     * 成员ID
     */
    private Integer unitId;
    /**
     * 成员相册ID
     */
    private Integer unitNumber;
    /**
     * 成员卡面ID
     */
    private Integer normalCardId;
    /**
     * 觉醒卡面ID
     */
    private Integer rankMaxCardId;
    /**
     * 卡名称
     */
    private String eponym;
    /**
     * 成员名称
     */
    private String name;

    public Unit() {
    }

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public Integer getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(Integer unitNumber) {
        this.unitNumber = unitNumber;
    }

    public Integer getNormalCardId() {
        return normalCardId;
    }

    public void setNormalCardId(Integer normalCardId) {
        this.normalCardId = normalCardId;
    }

    public Integer getRankMaxCardId() {
        return rankMaxCardId;
    }

    public void setRankMaxCardId(Integer rankMaxCardId) {
        this.rankMaxCardId = rankMaxCardId;
    }

    public String getEponym() {
        return eponym;
    }

    public void setEponym(String eponym) {
        this.eponym = eponym;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
