package entity.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final List<Map<PropertyEnum, Integer>> MAIN_PROPERTIES_WEIGHT = new ArrayList<>();
    private static final Map<PropertyEnum, Integer> SUB_PROPERTIES_WEIGHT = new HashMap<>();
    private static final Map<PropertyEnum, Double> SUB_PROPERTIES_RATIO = new HashMap<>();
    private static final double[] BASIC_SUB_VALUE = new double[] {2.72, 3.11, 3.50, 3.89};
    private static final double BASIC_MAIN_VALUE_MIN = 4.66;
    private static final double BASIC_MAIN_VALUE_LEVEL = 1.322;
    private static final double FLOWER_MAIN_VALUE_MIN = 717;
    private static final double FLOWER_MAIN_VALUE_LEVEL = 203.15;

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

        // 比值信息录入
        SUB_PROPERTIES_RATIO.put(PropertyEnum.HP, 76.8);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.ATK, 5.0);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.DEF, 6.0);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.HP_RATIO, 1.5);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.ATK_RATIO, 1.5);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.DEF_RATIO, 15.0 / 8);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.EM, 6.0);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.ENERGY, 5.0 / 3);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.CRITICAL_PROB, 1.0);
        SUB_PROPERTIES_RATIO.put(PropertyEnum.CRITICAL_DMG, 2.0);

    }

    public Saint(int pos, double criticalProb, double criticalDmg, double atkRatio, int atk) {
        this.pos = pos;
        this.subProperties = new ArrayList<>();
        this.subProperties.add(new Property(PropertyEnum.CRITICAL_PROB, criticalProb));
        this.subProperties.add(new Property(PropertyEnum.CRITICAL_DMG, criticalDmg));
        this.subProperties.add(new Property(PropertyEnum.ATK_RATIO, atkRatio));
        this.subProperties.add(new Property(PropertyEnum.ATK, atk));
        this.level = 20;
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
        double value;
        if (mainPropertyEnum.equals(PropertyEnum.HP)) {
            value = FLOWER_MAIN_VALUE_MIN;
        } else {
            value = SUB_PROPERTIES_RATIO.getOrDefault(mainPropertyEnum, 1.5) * BASIC_MAIN_VALUE_MIN;
            if (mainPropertyEnum.equals(PropertyEnum.ATK)) {
                value *= 2;
            }
        }

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
        if (this.mainProperty.getProperty().equals(PropertyEnum.HP)) {
            this.mainProperty.setValue(FLOWER_MAIN_VALUE_MIN + this.level * FLOWER_MAIN_VALUE_LEVEL);
        } else {
            this.mainProperty.setValue((BASIC_MAIN_VALUE_MIN + this.level * BASIC_MAIN_VALUE_LEVEL) *
                    SUB_PROPERTIES_RATIO.getOrDefault(this.mainProperty.getProperty(), 1.5));
            if (this.mainProperty.getProperty().equals(PropertyEnum.ATK)) {
                this.mainProperty.setValue(this.mainProperty.getValue() * 2);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (this.subProperties.size() < 4) {
            // 小于4属性，新增一属性
            Set<PropertyEnum> properties = this.subProperties.stream().map(Property::getProperty).collect(Collectors.toSet());
            properties.add(this.mainProperty.getProperty());
            PropertyEnum subPropertyEnum = randomChooseProperty(SUB_PROPERTIES_WEIGHT, properties);
            double value = BASIC_SUB_VALUE[RANDOM.nextInt(4)] * SUB_PROPERTIES_RATIO.get(subPropertyEnum);
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
            double value = BASIC_SUB_VALUE[RANDOM.nextInt(4)] * SUB_PROPERTIES_RATIO.get(subPropertyEnum);
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
        stringBuilder.append(this.name)
                .append("   [")
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
