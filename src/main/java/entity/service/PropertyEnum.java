package entity.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author guohaohao
 */
@Getter
@AllArgsConstructor
public enum PropertyEnum {

    HP("生命值", "%.0f"),
    ATK("攻击力", "%.0f"),
    DEF("防御力", "%.0f"),
    HP_RATIO("生命值", "%.1f%%"),
    ATK_RATIO("攻击力", "%.1f%%"),
    DEF_RATIO("防御力", "%.1f%%"),
    EM("元素精通", "%.0f"),
    ENERGY("元素充能效率", "%.1f%%"),
    CURE("治疗加成", "%.1f%%"),
    CRITICAL_PROB("暴击率", "%.1f%%"),
    CRITICAL_DMG("暴击伤害", "%.1f%%"),
    FIRE_DMG("火元素伤害加成", "%.1f%%"),
    WATER_DMG("水元素伤害加成", "%.1f%%"),
    ICE_DMG("冰元素伤害加成", "%.1f%%"),
    THUNDER_DMG("雷元素伤害加成", "%.1f%%"),
    WIND_DMG("风元素伤害加成", "%.1f%%"),
    ROCK_DMG("岩元素伤害加成", "%.1f%%"),
    PHYSICS_DMG("物理伤害加成", "%.1f%%");

    private String name;
    private String format;

}
