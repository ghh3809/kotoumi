package entity.service;

/**
 * @author guohaohao
 */
public class GenshinUnit {

    /**
     * 对象ID
     */
    private Long id;
    /**
     * 对象类型：1（角色）/2（武器）
     */
    private Integer unitType;
    /**
     * 对象名称
     */
    private String unitName;
    /**
     * 对象稀有度
     */
    private Integer rarity;
    /**
     * 精炼数/命之座数
     */
    private int level;

    public GenshinUnit() {
    }

    public GenshinUnit(GenshinUnit other) {
        this.id = other.id;
        this.unitType = other.unitType;
        this.unitName = other.unitName;
        this.rarity = other.rarity;
        this.level = other.level;
    }

    public GenshinUnit(Long id, Integer unitType, String unitName, Integer rarity) {
        this.id = id;
        this.unitType = unitType;
        this.unitName = unitName;
        this.rarity = rarity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUnitType() {
        return unitType;
    }

    public void setUnitType(Integer unitType) {
        this.unitType = unitType;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Integer getRarity() {
        return rarity;
    }

    public void setRarity(Integer rarity) {
        this.rarity = rarity;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
