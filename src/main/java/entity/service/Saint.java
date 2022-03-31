package entity.service;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author guohaohao
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Saint {

    public static final String[] POSITION_NAME = new String[] {"生之花", "死之羽", "时之沙", "空之杯", "理之冠"};
    public static final Map<PropertyEnum, Integer> SUB_PROPERTIES_WEIGHT = new HashMap<>();
    public static final Map<PropertyEnum, Double> MAIN_PROPERTIES_VALUE_0 = new HashMap<>();
    public static final Map<PropertyEnum, Double> MAIN_PROPERTIES_VALUE_20 = new HashMap<>();

    private static final List<Map<PropertyEnum, Integer>> MAIN_PROPERTIES_WEIGHT = new ArrayList<>();
    private static final Map<PropertyEnum, List<Double>> SUB_PROPERTIES_VALUE = new HashMap<>();
    private static final Random RANDOM = new Random();

    private int pos;
    private int level;
    private String name;
    private Property mainProperty;
    private List<Property> subProperties;
    private SaintScore saintScore;

    static {

        // 主词条信息录入
        // 生之花
        Map<PropertyEnum, Integer> weight = new HashMap<>();
        weight.put(PropertyEnum.HP, 1);
        MAIN_PROPERTIES_WEIGHT.add(weight);

        // 死之羽
        weight = new HashMap<>();
        weight.put(PropertyEnum.ATK, 1);
        MAIN_PROPERTIES_WEIGHT.add(weight);

        // 时之沙
        weight = new HashMap<>();
        weight.put(PropertyEnum.HP_RATIO, 8);
        weight.put(PropertyEnum.ATK_RATIO, 8);
        weight.put(PropertyEnum.DEF_RATIO, 8);
        weight.put(PropertyEnum.ENERGY, 3);
        weight.put(PropertyEnum.EM, 3);
        MAIN_PROPERTIES_WEIGHT.add(weight);

        // 空之杯
        weight = new HashMap<>();
        weight.put(PropertyEnum.HP_RATIO, 17);
        weight.put(PropertyEnum.ATK_RATIO, 17);
        weight.put(PropertyEnum.DEF_RATIO, 16);
        weight.put(PropertyEnum.EM, 2);
        weight.put(PropertyEnum.FIRE_DMG, 4);
        weight.put(PropertyEnum.WATER_DMG, 4);
        weight.put(PropertyEnum.ICE_DMG, 4);
        weight.put(PropertyEnum.THUNDER_DMG, 4);
        weight.put(PropertyEnum.WIND_DMG, 4);
        weight.put(PropertyEnum.ROCK_DMG, 4);
        weight.put(PropertyEnum.PHYSICS_DMG, 4);
        MAIN_PROPERTIES_WEIGHT.add(weight);

        // 理之冠
        weight = new HashMap<>();
        weight.put(PropertyEnum.HP_RATIO, 11);
        weight.put(PropertyEnum.ATK_RATIO, 11);
        weight.put(PropertyEnum.DEF_RATIO, 11);
        weight.put(PropertyEnum.CRITICAL_PROB, 5);
        weight.put(PropertyEnum.CRITICAL_DMG, 5);
        weight.put(PropertyEnum.CURE, 5);
        weight.put(PropertyEnum.EM, 2);
        MAIN_PROPERTIES_WEIGHT.add(weight);

        // 副词条信息录入
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.HP, 6);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.ATK, 6);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.DEF, 6);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.HP_RATIO, 4);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.ATK_RATIO, 4);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.DEF_RATIO, 4);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.EM, 4);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.ENERGY, 4);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.CRITICAL_PROB, 3);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.CRITICAL_DMG, 3);

        // 副词条数值录入
        SUB_PROPERTIES_VALUE.put(PropertyEnum.HP, Arrays.asList(209.13, 239.00, 268.88, 298.75));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.ATK, Arrays.asList(13.62, 15.56, 17.51, 19.45));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.DEF, Arrays.asList(16.20, 18.52, 20.83, 23.15));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.HP_RATIO, Arrays.asList(4.08, 4.66, 5.25, 5.83));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.ATK_RATIO, Arrays.asList(4.08, 4.66, 5.25, 5.83));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.DEF_RATIO, Arrays.asList(5.10, 5.83, 6.56, 7.29));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.EM, Arrays.asList(16.32, 18.65, 20.98, 23.31));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.ENERGY, Arrays.asList(4.53, 5.18, 5.83, 6.48));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.CRITICAL_PROB, Arrays.asList(2.72, 3.11, 3.50, 3.89));
        SUB_PROPERTIES_VALUE.put(PropertyEnum.CRITICAL_DMG, Arrays.asList(5.44, 6.22, 6.99, 7.77));

        // 主词条数值录入
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.HP, 717.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.ATK, 47.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.HP_RATIO, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.ATK_RATIO, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.DEF_RATIO, 8.7);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.EM, 28.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.ENERGY, 7.8);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.FIRE_DMG, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.WATER_DMG, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.ICE_DMG, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.THUNDER_DMG, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.WIND_DMG, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.ROCK_DMG, 7.0);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.PHYSICS_DMG, 8.7);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.CRITICAL_PROB, 4.7);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.CRITICAL_DMG, 9.3);
        MAIN_PROPERTIES_VALUE_0.put(PropertyEnum.CURE, 5.4);

        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.HP, 4780.0);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.ATK, 311.0);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.HP_RATIO, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.ATK_RATIO, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.DEF_RATIO, 58.3);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.EM, 187.0);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.ENERGY, 51.8);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.FIRE_DMG, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.WATER_DMG, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.ICE_DMG, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.THUNDER_DMG, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.WIND_DMG, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.ROCK_DMG, 46.6);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.PHYSICS_DMG, 58.3);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.CRITICAL_PROB, 31.1);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.CRITICAL_DMG, 62.2);
        MAIN_PROPERTIES_VALUE_20.put(PropertyEnum.CURE, 35.9);

    }

    public Saint(int pos, int level) {
        this.pos = pos;
        this.subProperties = new ArrayList<>();
        this.level = level;
    }

    public Saint(DbSaint dbSaint) {
        this.pos = dbSaint.getPos();
        this.level = dbSaint.getLevel();
        this.name = dbSaint.getSaintName();
        this.mainProperty = JSON.parseObject(dbSaint.getMainProperty(), Property.class);
        this.subProperties = JSON.parseArray(dbSaint.getSubProperties(), Property.class);
    }

    /**
     * 随机生成一个圣遗物
     * @param saintSuitList 套装列表
     * @return 生成的圣遗物
     */
    public static Saint generateSaint(List<SaintSuit> saintSuitList) {
        int pos = RANDOM.nextInt(5);
        PropertyEnum mainPropertyEnum = randomChooseProperty(MAIN_PROPERTIES_WEIGHT.get(pos), null);
        double value = MAIN_PROPERTIES_VALUE_0.get(mainPropertyEnum);

        Property mainProperty = new Property(mainPropertyEnum, value);
        List<Property> subProperties = new ArrayList<>();
        Saint saint = Saint.builder()
                .pos(pos)
                .mainProperty(mainProperty)
                .name(chooseName(saintSuitList, pos))
                .level(0)
                .subProperties(subProperties)
                .build();

        for (int i = 0; i < 3; i ++) {
            saint.strengthen(false);
        }
        if (RANDOM.nextDouble() < 0.2) {
            saint.strengthen(false);
        }

        return saint;
    }

    /**
     * 圣遗物强化
     */
    public String strengthen() {
        return strengthen(true);
    }

    /**
     * 圣遗物强化
     * @param lvup 是否强化等级
     */
    private String strengthen(boolean lvup) {

        if (lvup) {
            this.level += 4;
        }
        if (lvup) {
            this.mainProperty.setValue(MAIN_PROPERTIES_VALUE_0.get(this.mainProperty.getProperty()) +
                    0.05 * this.level * (MAIN_PROPERTIES_VALUE_20.get(this.mainProperty.getProperty()) -
                            MAIN_PROPERTIES_VALUE_0.get(this.mainProperty.getProperty())));
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (this.subProperties.size() < 4) {
            // 小于4属性，新增一属性
            Set<PropertyEnum> properties = this.subProperties.stream().map(Property::getProperty).collect(Collectors.toSet());
            properties.add(this.mainProperty.getProperty());
            PropertyEnum subPropertyEnum = randomChooseProperty(SUB_PROPERTIES_WEIGHT, properties);
            double value = SUB_PROPERTIES_VALUE.get(subPropertyEnum).get(RANDOM.nextInt(4));
            this.subProperties.add(new Property(subPropertyEnum, value));
            stringBuilder.append(subPropertyEnum.getName())
                    .append("\t    \t->\t")
                    .append(String.format(subPropertyEnum.getFormat(), value))
                    .append("\n");
        } else {
            // 大于4属性，进行随机强化
            Map<PropertyEnum, Integer> weightMap = new HashMap<>();
            for (Property subProperty : this.subProperties) {
                weightMap.put(subProperty.getProperty(), 1);
            }
            PropertyEnum subPropertyEnum = randomChooseProperty(weightMap, null);
            double value = SUB_PROPERTIES_VALUE.get(subPropertyEnum).get(RANDOM.nextInt(4));
            for (Property subProperty : this.subProperties) {
                if (subProperty.getProperty().equals(subPropertyEnum)) {
                    stringBuilder.append(subPropertyEnum.getName())
                            .append("\t")
                            .append(String.format(subPropertyEnum.getFormat(), subProperty.getValue()))
                            .append("\t->\t");
                    subProperty.setValue(subProperty.getValue() + value);
                    stringBuilder.append(String.format(subPropertyEnum.getFormat(), subProperty.getValue()))
                            .append("\n");
                    break;
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 根据权重，随机选择一个属性
     * @param weight 权重map
     * @return 选取的属性
     */
    public static PropertyEnum randomChooseProperty(Map<PropertyEnum, Integer> weight, Set<PropertyEnum> ignoreProperties) {
        List<PropertyEnum> chooseList = new ArrayList<>();
        List<Integer> chooseWeightList = new ArrayList<>();
        int totalWeight = 0;
        for (Map.Entry<PropertyEnum, Integer> entry : weight.entrySet()) {
            if (ignoreProperties == null || !ignoreProperties.contains(entry.getKey())) {
                chooseList.add(entry.getKey());
                totalWeight += entry.getValue();
                chooseWeightList.add(totalWeight);
            }
        }

        int chooseWeight = RANDOM.nextInt(totalWeight);
        for (int i = 0; i < chooseList.size(); i ++) {
            if (chooseWeightList.get(i) > chooseWeight) {
                return chooseList.get(i);
            }
        }
        return chooseList.get(chooseList.size() - 1);
    }

    /**
     * 选择名称
     * @param saintSuitList 套装信息列表
     * @param pos 位置
     * @return 圣遗物名称
     */
    private static String chooseName(List<SaintSuit> saintSuitList, int pos) {
        List<SaintSuit> filterList = saintSuitList.stream().filter(o -> o.getPos() == pos).collect(Collectors.toList());
        return filterList.get(RANDOM.nextInt(filterList.size())).getSaintName();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.name == null ? "" : (this.name + "   "))
                .append("[")
                .append(POSITION_NAME[this.pos])
                .append(" +")
                .append(this.level)
                .append("]\n----------\n")
                .append(this.mainProperty)
                .append("\n----------\n");
        for (Property subProperty : subProperties) {
            stringBuilder.append(subProperty).append("\n");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

}
