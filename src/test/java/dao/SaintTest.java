package dao;

import com.alibaba.fastjson.JSON;
import entity.service.Property;
import entity.service.PropertyEnum;
import entity.service.Saint;
import entity.service.SaintSuit;
import entity.service.WishStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import utils.SaintHelper;
import utils.WishHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SaintTest {

    @Test
    public void testScore() {
        Saint saint = new Saint(0, 3.9, 7.8, 0, 0);
        saint.setMainProperty(new Property(PropertyEnum.HP, 311));
        saint.setName("雷鸟的怜悯");
        log.info(JSON.toJSONString(SaintHelper.score(saint)));
        log.info("\n" + saint);
    }

    @Test
    public void testRandom() {
        Map<PropertyEnum, Integer> SUB_PROPERTIES_WEIGHT = new HashMap<>();
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.HP, 6);
        SUB_PROPERTIES_WEIGHT.put(PropertyEnum.ATK, 2);
        int count1 = 0, count2 = 0;
        for (int i = 0; i < 1000; i ++) {
            PropertyEnum propertyEnum = Saint.randomChooseProperty(SUB_PROPERTIES_WEIGHT, null);
            if (propertyEnum.equals(PropertyEnum.ATK)) {
                count1 ++;
            } else {
                count2 ++;
            }
        }
        log.info("ATK = {}, HP = {}", count1, count2);
    }

    @Test
    public void testGenerateSaint() {
        List<SaintSuit> saintSuitList = new ArrayList<>();
        saintSuitList.add(new SaintSuit(1L, "如雷", 0, "如雷花"));
        saintSuitList.add(new SaintSuit(1L, "如雷", 1, "如雷羽毛"));
        saintSuitList.add(new SaintSuit(1L, "如雷", 2, "如雷沙"));
        saintSuitList.add(new SaintSuit(1L, "如雷", 3, "如雷杯子"));
        saintSuitList.add(new SaintSuit(1L, "如雷", 4, "如雷头"));

        int[] count = new int[] {0, 0, 0, 0, 0};
        ArrayList<Double> flowerScore = new ArrayList<>();
        for (int i = 0; i < 100000; i ++) {
            Saint saint = Saint.generateSaint(saintSuitList);
            count[saint.getPos()] ++;
            saint.strengthen();
            saint.strengthen();
            saint.strengthen();
            saint.strengthen();
            saint.strengthen();
            SaintHelper.score(saint);
            if (saint.getPos() == 0) {
                flowerScore.add(saint.getSaintScore().getScore());
            }
        }
        log.info(Arrays.toString(count));
        flowerScore.sort(Double::compare);
        log.info("50% = {}", flowerScore.get((int) (count[0] * 0.5)));
        log.info("90% = {}", flowerScore.get((int) (count[0] * 0.9)));
        log.info("95% = {}", flowerScore.get((int) (count[0] * 0.95)));
        log.info("99% = {}", flowerScore.get((int) (count[0] * 0.99)));
        log.info("99.9% = {}", flowerScore.get((int) (count[0] * 0.999)));
    }

}
