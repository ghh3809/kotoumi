package constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author guohaohao
 */
@AllArgsConstructor
@Getter
public enum UnitType {

    /**
     * 类型信息
     */
    CHARACTER(1, "角色", 7, 1, "命"),
    WEAPON(2, "武器", 5, 0, "精炼"),
    FRIEND(3, "群友", 7, 1, "命"),
    REWARD(4, "奖品", 100, 0, "个");

    /**
     * 类型ID，默认角色为1，武器为2
     */
    private int id;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 溢出需要的次数
     */
    private int fullSize;

    /**
     * 初始数量，角色需要减去1次（0命座）
     */
    private int levelBias;

    /**
     * 单位名称，例如命/精炼
     */
    private String unitName;

    /**
     * 根据类型ID，获取类型信息
     * @param id 类型ID
     * @return 类型信息
     */
    public static UnitType getById(int id) {
        for (UnitType unitType : UnitType.values()) {
            if (unitType.getId() == id) {
                return unitType;
            }
        }
        return null;
    }

    /**
     * 根据类型名称，获取类型信息
     * @param name 类型名称
     * @return 类型信息
     */
    public static UnitType getByName(String name) {
        for (UnitType unitType : UnitType.values()) {
            if (unitType.getTypeName().equals(name)) {
                return unitType;
            }
        }
        return null;
    }

}
