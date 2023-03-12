package dao;

import com.alibaba.fastjson.JSON;
import entity.service.Property;
import entity.service.PropertyEnum;
import entity.service.Saint;
import entity.service.SaintSuit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import processor.ChatGPTService;
import utils.SaintHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SaintTest {

    @Test
    public void testScore() {
        Saint saint = new Saint(0, 0);
        saint.setMainProperty(new Property(PropertyEnum.HP, 311));
        saint.setName("雷鸟的怜悯");
        saint.getSubProperties().add(new Property(PropertyEnum.ATK_RATIO, 5.25));
        saint.getSubProperties().add(new Property(PropertyEnum.DEF_RATIO, 16.20));
        saint.getSubProperties().add(new Property(PropertyEnum.CRITICAL_DMG, 3.89));
        saint.getSubProperties().add(new Property(PropertyEnum.ENERGY, 4.53));
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

        int count = 0;
        for (int i = 0; i < 1000000; i ++) {
            Saint saint = Saint.generateSaint(saintSuitList);
            saint.strengthen();
            int effect = 0;
            if (saint.getMainProperty().getProperty().equals(PropertyEnum.CRITICAL_PROB) || saint.getMainProperty().getProperty().equals(PropertyEnum.CRITICAL_DMG)) {
                effect ++;
            }
            for (Property subProperty : saint.getSubProperties()) {
                if (subProperty.getProperty().equals(PropertyEnum.CRITICAL_PROB) || subProperty.getProperty().equals(PropertyEnum.CRITICAL_DMG)) {
                    effect ++;
                }
            }
            if (effect == 2) {
                count ++;
            }

        }
        log.info("双爆圣遗物比例：{}%", count * 100.0 / 1000000);
    }

    @Test
    public void testUnit() {
        log.info(utterance());
    }

    @Test
    public void testChatGPT() {
        String result = ChatGPTService.dialog("123", "hello");
        log.info(result);
    }

    private String utterance() {
        // 请求URL
        String talkUrl = "https://aip.baidubce.com/rpc/2.0/unit/bot/chat";
        try {
            // 请求参数
            String params = "{\"bot_id\": \"1057439\",\"bot_session\": \"\",\"log_id\": \"UNITTEST_20210604144315_1493400726\",\"request\": {\"query\": \"测试\",\"user_id\": \"1452630069_1\"},\"version\": \"2.0\"}";
            String accessToken = "24.a987331387e146e21d70dce59a069125.2592000.1628253263.282335-16827442";
            String result = HttpUtil.post(talkUrl, accessToken, "application/json", params);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
