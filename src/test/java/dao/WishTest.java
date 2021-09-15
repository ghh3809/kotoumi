package dao;

import entity.service.WishStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import utils.WishHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WishTest {

    @Test
    public void testRegx() {
        Pattern SAINT_SCORE_PATTERN = Pattern.compile("^.*?位置：(.*?)\\n暴击率：([0-9.]+)%?\\n暴击伤害：([0-9.]+)%?\\n攻击力百分比：([0-9.]+)%?\\n攻击力：([0-9.]+).*$", Pattern.DOTALL);
        String str = "@风之筝 (请复制本消息并填写副词条信息，发送回群中)\n" +
                "【圣遗物评分】\n" +
                "位置：花\n" +
                "暴击率：3.9%\n" +
                "暴击伤害：7.8%\n" +
                "攻击力百分比：10.5%\n" +
                "攻击力：0";
        Matcher matcher = SAINT_SCORE_PATTERN.matcher(str);
        log.info("find: {}", matcher.find());
    }

    @Test
    public void testWish() {
        Random random = new Random();
        WishStatus wishStatus = new WishStatus();
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int i = 0; i < 80; i ++) {
            countMap.put(i + 1, 0);
        }
        int count5 = 0, count4 = 0, total = 0;
        for (int i = 0; i < 1000000000; i ++) {
            double prob4 = WishHelper.getNextProb4(wishStatus, 2);
            double prob5 = WishHelper.getNextProb5(wishStatus, 2);
            double current = random.nextDouble();
            total ++;
//            log.info("prob5: {}, prob4: {}, current: {}", prob5, prob4, current);
            if (current < prob5) {
//                log.info("Get 5");
                int times = wishStatus.getStar5Count() + 1;
                countMap.put(times, countMap.get(times) + 1);
                wishStatus.setStar4Count(0);
                wishStatus.setStar5Count(0);
                count5 ++;
            } else if (current < prob5 + prob4) {
//                log.info("Get 4");
                wishStatus.setStar4Count(0);
                wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                count4 ++;
            } else {
                wishStatus.setStar4Count(wishStatus.getStar4Count() + 1);
                wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
            }
        }
        log.info("total: {}, count5: {}, count4: {}", total, count5, count4);
        for (int i = 0; i < 80; i ++) {
            log.info("Times = " + (i + 1) + ", Count = " + countMap.get(i + 1));
        }
    }

    @Test
    public void testWish2() {
        // 每单能抽多少发
        double wishFrom648 = 8080.0 / 160;
        double value5 = 62.5 / wishFrom648 * 648;
        int cost = 3300;
        int minPayTime = 4;
        boolean isWell = true;
        Random random = new Random();
        Map<Integer, Integer> upCountMap = new HashMap<>();

        int totalI = 10000000;
        double totalAmount = 0;
        for (int i = 0; i < totalI; i ++) {
            WishStatus wishStatus = new WishStatus();
            // 是否为大保底
            boolean well = isWell;
            int upCount = 0;
            int totalWish = 0;
            while (true) {
                double prob4 = WishHelper.getNextProb4(wishStatus, 3);
                double prob5 = WishHelper.getNextProb5(wishStatus, 3);
                double current = random.nextDouble();
                if (current < prob5) {
                    totalWish += wishStatus.getStar5Count() + 1;
//                    log.info("currentWish = {}", wishStatus.getStar5Count() + 1);
                    if (well || random.nextBoolean()) {
                        upCount ++;
                        well = false;
                    } else {
                        double payTime = Math.ceil(totalWish / wishFrom648);
                        if (payTime < minPayTime) {
                            payTime = minPayTime;
                        }
                        double remain = payTime * 648 - totalWish / wishFrom648 * 648;
                        double amount = upCount * value5 * 1.5 + value5 * 0.5 + remain - cost;
                        if (isWell) {
                            amount -= value5 * 0.5;
                        }
//                        log.info("upCount = {}, totalWish = {}, payTime = {}, remain = {}, amount = {}", upCount, totalWish, payTime, remain, amount);
                        totalAmount += amount;
                        upCountMap.put(upCount, upCountMap.getOrDefault(upCount, 0) + 1);
                        break;
                    }
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(0);
                } else if (current < prob5 + prob4) {
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                } else {
                    wishStatus.setStar4Count(wishStatus.getStar4Count() + 1);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                }
            }
        }

        for (int i = 0; i < 10; i ++) {
            log.info("upCount = {}, count = {}", i, upCountMap.getOrDefault(i, 0));
        }

        log.info("Avg amount = {}", totalAmount / totalI);
    }

    @Test
    public void testWish3() {
        Random random = new Random();

        Map<Integer, Integer> countMap = new HashMap<>();
        Map<Integer, Integer> countMap2 = new HashMap<>();

        int count5 = 0, count4 = 0, total = 0;
        for (int i = 0; i < 1000000; i ++) {
            WishStatus wishStatus = new WishStatus();
            int times = 0;
            boolean flag = false;
            while (true) {
                double prob4 = WishHelper.getNextProb4(wishStatus, 1);
                double prob5 = WishHelper.getNextProb5(wishStatus, 1);
                double current = random.nextDouble();
                total ++;
                if (current < prob5) {
                    if (random.nextBoolean() || flag) {
                        times += wishStatus.getStar5Count() + 1;
//                        log.info("{}中了{}", i, times);
                        countMap2.put(times, countMap2.getOrDefault(times, 0) + 1);
                        break;
                    }

                    times += wishStatus.getStar5Count() + 1;
//                    log.info("{}歪了{}", i, times);
                    countMap.put(times, countMap.getOrDefault(times, 0) + 1);
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(0);
                    count5 ++;
                    flag = true;
                } else if (current < prob5 + prob4) {
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                    count4 ++;
                } else {
                    wishStatus.setStar4Count(wishStatus.getStar4Count() + 1);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                }
            }
        }
        log.info("total: {}, count5: {}, count4: {}", total, count5, count4);
        for (int i = 0; i < 90; i ++) {
            log.info("Times = " + (i + 1) + ", Count = " + countMap.getOrDefault(i + 1, 0));
        }
        for (int i = 0; i < 180; i ++) {
            log.info("Total Times = " + (i + 1) + ", Count = " + countMap2.getOrDefault(i + 1, 0));
        }
    }

    @Test
    public void testWish4() {
        Random random = new Random();
        int target = 1;
        Map<Integer, Integer> countMap2 = new HashMap<>();

        for (int i = 0; i < 10000000; i ++) {
            WishStatus wishStatus = new WishStatus();
            int times = 0;
            int upCount = 0;
            boolean flag = false;
            while (true) {
                double prob4 = WishHelper.getNextProb4(wishStatus, 1);
                double prob5 = WishHelper.getNextProb5(wishStatus, 1);
                double current = random.nextDouble();
                if (current < prob5) {
                    if (flag || random.nextBoolean()) {
                        upCount ++;
                        flag = false;
                    } else {
                        flag = true;
                    }
                    times += wishStatus.getStar5Count() + 1;
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(0);
                    if (upCount == target) {
                        countMap2.put(times, countMap2.getOrDefault(times, 0) + 1);
                        break;
                    }

                } else if (current < prob5 + prob4) {
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                } else {
                    wishStatus.setStar4Count(wishStatus.getStar4Count() + 1);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                }
            }
        }
        for (int i = 0; i < target * 180; i ++) {
            log.info("Total Times = " + (i + 1) + ", Count = " + countMap2.getOrDefault(i + 1, 0));
        }
    }

    @Test
    public void testWish5() {
        Random random = new Random();
        int target = 3;
        Map<Integer, Integer> countMap2 = new HashMap<>();

        for (int i = 0; i < 10000000; i ++) {
            WishStatus wishStatus = new WishStatus();
            int times = 0;
            int start5Count = 0;
            int upCount = 0;
            boolean flag = false;
            while (true) {
                double prob4 = WishHelper.getNextProb4(wishStatus, 2);
                double prob5 = WishHelper.getNextProb5(wishStatus, 2);
                double current = random.nextDouble();
                if (current < prob5) {
                    double tmp = random.nextDouble();
                    start5Count ++;
                    if (start5Count == 3) {
                        // 3次金，必定为up武器
                        upCount ++;
                        flag = false;
                    } else if (flag) {
                        // 大保底，则重置为小保底，up概率50%
                        flag = false;
                        if (tmp < 0.5) {
                            upCount ++;
                        }
                    } else {
                        // 小保底，37.5%up武器，37.5%另一把武器，25%编程大保底
                        if (tmp < 0.375) {
                            upCount ++;
                            flag = false;
                        } else {
                            flag = tmp >= 0.75;
                        }
                    }
                    times += wishStatus.getStar5Count() + 1;
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(0);
                    if (upCount == target) {
                        countMap2.put(times, countMap2.getOrDefault(times, 0) + 1);
                        break;
                    }

                } else if (current < prob5 + prob4) {
                    wishStatus.setStar4Count(0);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                } else {
                    wishStatus.setStar4Count(wishStatus.getStar4Count() + 1);
                    wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                }
            }
        }
        for (int i = 0; i < target * 240; i ++) {
            log.info("Total Times = " + (i + 1) + ", Count = " + countMap2.getOrDefault(i + 1, 0));
        }
    }

}
