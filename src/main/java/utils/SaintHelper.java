package utils;

import entity.service.Property;
import entity.service.PropertyEnum;
import entity.service.Saint;
import entity.service.SaintScore;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guohaohao
 */
@Slf4j
public class SaintHelper {

    private static final List<List<List<Double>>> SAINT_RATIO = new ArrayList<>();
    private static final List<List<PropertyEnum>> USEFUL_MAIN_PROPERTIES = new ArrayList<>();

    static {
        // 录入有效主词条信息
        List<PropertyEnum> usefulProperties = new ArrayList<>();
        usefulProperties.add(PropertyEnum.HP);
        USEFUL_MAIN_PROPERTIES.add(usefulProperties);

        usefulProperties = new ArrayList<>();
        usefulProperties.add(PropertyEnum.ATK);
        USEFUL_MAIN_PROPERTIES.add(usefulProperties);

        usefulProperties = new ArrayList<>();
        usefulProperties.add(PropertyEnum.ATK_RATIO);
        USEFUL_MAIN_PROPERTIES.add(usefulProperties);

        usefulProperties = new ArrayList<>();
        usefulProperties.add(PropertyEnum.FIRE_DMG);
        usefulProperties.add(PropertyEnum.WATER_DMG);
        usefulProperties.add(PropertyEnum.ICE_DMG);
        usefulProperties.add(PropertyEnum.THUNDER_DMG);
        usefulProperties.add(PropertyEnum.WIND_DMG);
        usefulProperties.add(PropertyEnum.ROCK_DMG);
        usefulProperties.add(PropertyEnum.PHYSICS_DMG);
        USEFUL_MAIN_PROPERTIES.add(usefulProperties);

        usefulProperties = new ArrayList<>();
        usefulProperties.add(PropertyEnum.CRITICAL_PROB);
        usefulProperties.add(PropertyEnum.CRITICAL_DMG);
        USEFUL_MAIN_PROPERTIES.add(usefulProperties);

        // 导入评分数据
        File file = new File("data/saint_ratio.txt");
        List<String> lines = FileHelper.readLines(file);
        for (String line : lines) {
            String[] parts = line.split("\t");
            if (parts.length == 7) {
                List<Double> singleRatio = new ArrayList<>(6);
                for (int i = 1; i < 7; i ++) {
                    singleRatio.add(Double.parseDouble(parts[i]));
                }
                if (parts[1].startsWith("0")) {
                    SAINT_RATIO.add(new ArrayList<>());
                }
                SAINT_RATIO.get(SAINT_RATIO.size() - 1).add(singleRatio);
            }
        }
    }

    /**
     * 根据位置描述，获取圣遗物位置代码
     * @param pos 位置描述
     * @return 圣遗物位置代码
     */
    public static int getPos(String pos) throws Exception {
        if (pos.contains("花")) {
            return 0;
        } else if (pos.contains("羽")) {
            return 1;
        } else if (pos.contains("沙")) {
            return 2;
        } else if (pos.contains("杯")) {
            return 3;
        } else if (pos.contains("头") || pos.contains("冠")) {
            return 4;
        } else {
            throw new Exception("圣遗物位置无法识别");
        }
    }

    /**
     * 进行圣遗物评分
     * @param saint 圣遗物
     * @return 评分结果
     */
    public static SaintScore score(Saint saint) {
        double score = getScore(saint);
        double levelScore = getLevelScore(saint);
        log.info("Level score = {}", levelScore);
        double ratio = getRatio(saint.getLevel(), score + levelScore, saint.getPos());
        long value = getValue(ratio);
        String level = getLevel(ratio);
        SaintScore saintScore = new SaintScore(
                score,
                levelScore,
                ratio,
                value,
                level
        );
        saint.setSaintScore(saintScore);
        return saintScore;
    }

    /**
     * 获取圣遗物双暴攻击
     * @param saint 圣遗物
     * @return 双暴攻击
     */
    private static double getScore(Saint saint) {
        double score = 0;
        if (saint.getMainProperty() != null &&
                !USEFUL_MAIN_PROPERTIES.get(saint.getPos()).contains(saint.getMainProperty().getProperty())) {
            return -1;
        }
        for (Property subProperty : saint.getSubProperties()) {
            if (subProperty.getProperty().equals(PropertyEnum.CRITICAL_PROB)) {
                score += subProperty.getValue() * 2;
            } else if (subProperty.getProperty().equals(PropertyEnum.CRITICAL_DMG)) {
                score += subProperty.getValue();
            } else if (subProperty.getProperty().equals(PropertyEnum.ATK_RATIO)) {
                score += subProperty.getValue();
            } else if (subProperty.getProperty().equals(PropertyEnum.ATK)) {
                score += subProperty.getValue() * 0.15;
            }
        }
        return score;
    }

    /**
     * 获取圣遗物由于等级获得的附加得分
     * @param saint 圣遗物
     * @return 附加得分
     */
    private static double getLevelScore(Saint saint) {
        double score = 0;
        List<PropertyEnum> subProperties = saint.getSubProperties().stream()
                .map(Property::getProperty).collect(Collectors.toList());
        if (saint.getMainProperty() == null) {
            PropertyEnum mainPropertyEnum;
            switch (saint.getPos()) {
                case 0:
                    mainPropertyEnum = PropertyEnum.HP;
                    break;
                case 1:
                    mainPropertyEnum = PropertyEnum.ATK;
                    break;
                case 2:
                    mainPropertyEnum = PropertyEnum.ATK_RATIO;
                    break;
                case 3:
                    mainPropertyEnum = PropertyEnum.FIRE_DMG;
                    break;
                case 4:
                    if (subProperties.contains(PropertyEnum.CRITICAL_DMG)) {
                        mainPropertyEnum = PropertyEnum.CRITICAL_PROB;
                    } else {
                        mainPropertyEnum = PropertyEnum.CRITICAL_DMG;
                    }
                    break;
                default:
                    mainPropertyEnum = null;
            }
            saint.setMainProperty(new Property(mainPropertyEnum, Saint.MAIN_PROPERTIES_VALUE_0.get(mainPropertyEnum)));
        }
        if (!USEFUL_MAIN_PROPERTIES.get(saint.getPos()).contains(saint.getMainProperty().getProperty())) {
            return -1;
        }

        if (saint.getSubProperties().size() == 3) {
            int totalWeight = 0;
            for (Map.Entry<PropertyEnum, Integer> entry : Saint.SUB_PROPERTIES_WEIGHT.entrySet()) {
                PropertyEnum propertyEnum = entry.getKey();
                Integer weight = entry.getValue();
                if (!subProperties.contains(propertyEnum) && !saint.getMainProperty().getProperty().equals(propertyEnum)) {
                    subProperties.add(propertyEnum);
                    score += getLevelScoreByProperties(subProperties) * weight;
                    totalWeight += weight;
                    subProperties.remove(3);
                }
            }
            score /= totalWeight;
        } else {
            score = getLevelScoreByProperties(subProperties);
        }
        return (23 - saint.getLevel()) / 4 * score;
    }

    /**
     * 根据属性值计算等级附加得分
     * @param properties 属性值
     * @return 等级附加分
     */
    private static double getLevelScoreByProperties(List<PropertyEnum> properties) {
        double score = 0;
        for (PropertyEnum propertyEnum : properties) {
            if (propertyEnum.equals(PropertyEnum.CRITICAL_PROB)) {
                score += 1.6525;
            } else if (propertyEnum.equals(PropertyEnum.CRITICAL_DMG)) {
                score += 1.65125;
            } else if (propertyEnum.equals(PropertyEnum.ATK_RATIO)) {
                score += 1.23875;
            } else if (propertyEnum.equals(PropertyEnum.ATK)) {
                score += 0.6200625;
            }
        }
        return score;
    }

    /**
     * 获取圣遗物分位
     * @param score 圣遗物得分
     * @param pos 圣遗物位置
     * @return 圣遗物分位
     */
    private static double getRatio(int level, double score, int pos) {
        double ratio = 0;
        for (List<Double> singleRatio : SAINT_RATIO.get(level / 4)) {
            if (score - singleRatio.get(pos + 1) > 0.005) {
                ratio = singleRatio.get(0);
            } else {
                break;
            }
        }
        return ratio;
    }

    /**
     * 获取圣遗物体力价值
     * @param ratio 圣遗物分位
     * @return 体力价值
     */
    private static long getValue(double ratio) {
        return (long) (10000 / 1.065 / (100 - ratio));
    }

    /**
     * 获取圣遗物评级
     * @param ratio 圣遗物分位
     * @return 评级
     */
    public static String getLevel(double ratio) {
        String level;
        if (ratio < 50) {
            level = "D";
        } else if (ratio < 80) {
            level = "C";
        } else if (ratio < 90) {
            level = "B";
        } else if (ratio < 95) {
            level = "A";
        } else if (ratio < 99) {
            level = "S";
        } else if (ratio < 99.9) {
            level = "S+";
        } else if (ratio < 99.99) {
            level = "S++";
        } else if (ratio < 99.999) {
            level = "S+++";
        } else if (ratio < 99.9999) {
            level = "SS";
        } else if (ratio < 99.99999) {
            level = "SS+";
        } else if (ratio < 99.999999) {
            level = "SS++";
        } else {
            level = "SS+++";
        }
        return level;
    }

}
