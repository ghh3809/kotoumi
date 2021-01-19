package utils;

import com.alibaba.fastjson.JSON;
import dao.Dao;
import entity.service.GenshinUnit;
import entity.service.WishStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author guohaohao
 */
@Slf4j
public class WishHelper {

    public static final Random RAMDOM = new Random();
    public static final List<GenshinUnit> STAR_3_WEAPON = new ArrayList<>();
    public static final List<GenshinUnit> STAR_4_WEAPON = new ArrayList<>();
    public static final List<GenshinUnit> STAR_5_WEAPON = new ArrayList<>();
    public static final List<GenshinUnit> STAR_4_CHARACTER = new ArrayList<>();
    public static final List<GenshinUnit> STAR_5_CHARACTER = new ArrayList<>();

    static {
        List<GenshinUnit> allUnits = Dao.getUnits();
        log.info("Load units: {}", allUnits.size());
        for (GenshinUnit unit : allUnits) {
            if (unit.getRarity() == 3) {
                STAR_3_WEAPON.add(unit);
            } else if (unit.getRarity() == 4) {
                if (unit.getUnitType() == 1) {
                    STAR_4_CHARACTER.add(unit);
                } else {
                    STAR_4_WEAPON.add(unit);
                }
            } else {
                if (unit.getUnitType() == 1) {
                    STAR_5_CHARACTER.add(unit);
                } else {
                    STAR_5_WEAPON.add(unit);
                }
            }
        }
    }

    /**
     * 进行祈愿
     * @param wishCount 祈愿次数，1/10
     * @param wishType 祈愿类型，1（角色）/2（武器）/3（混合）
     * @param userId 用户ID
     * @return 祈愿结果
     */
    public static List<GenshinUnit> wish(int wishCount, int wishType, long userId, WishStatus wishStatus) {
        // 获取当前池子祈愿状态，更新保底次数
        List<GenshinUnit> wishHistory = Dao.getWishHistory(userId, wishType, 0);
        updateWishStatus(wishHistory, wishStatus);
        Collections.reverse(wishHistory);

        // 获取总祈愿状态，获得命座情况
        List<GenshinUnit> allHistory = Dao.getWishHistory(userId, 0, 0);
        Map<Long, Integer> summary = getSummary(allHistory);

        // 祈愿
        List<GenshinUnit> wishResult = new ArrayList<>();
        for (int i = 0; i < wishCount; i ++) {

            // 获取抽卡概率
            double nextProb4 = getNextProb4(wishStatus);
            double nextProb5 = getNextProb5(wishStatus);
            double rand = RAMDOM.nextDouble();
            GenshinUnit unit;

            // 进行抽卡
            if (rand < nextProb5) {
                // 抽到五星
                List<GenshinUnit> available = STAR_5_CHARACTER;
                if (wishType == 2 || (wishType == 3 && RAMDOM.nextDouble() < 0.5)) {
                    available = STAR_5_WEAPON;
                }
                unit = chooseOne(available);

                // 更新抽卡状态
                wishStatus.setStar5Count(0);
                wishStatus.setStar4Count(0);
            } else if (rand < nextProb4 + nextProb5) {
                // 抽到四星
                List<GenshinUnit> available = STAR_4_CHARACTER;
                if (wishType == 2 || (wishType == 3 && RAMDOM.nextDouble() < 0.5)) {
                    available = STAR_4_WEAPON;
                }
                unit = chooseOne(available);

                // 更新抽卡状态
                wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                wishStatus.setStar4Count(0);
            } else {
                // 抽到三星
                unit = chooseOne(STAR_3_WEAPON);

                // 更新抽卡状态
                wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                wishStatus.setStar4Count(wishStatus.getStar4Count() + 1);
            }

            // 更新精炼/命座信息和获得星辉信息
            if (!summary.containsKey(unit.getId())) {
                summary.put(unit.getId(), 1);
            } else {
                summary.put(unit.getId(), summary.get(unit.getId()) + 1);
            }
            unit.setLevel(summary.get(unit.getId()));
            if (unit.getRarity() == 5) {
                if (isOverflow(unit)) {
                    wishStatus.setTotalStarLight(wishStatus.getTotalStarLight() + 25);
                } else {
                    wishStatus.setTotalStarLight(wishStatus.getTotalStarLight() + 10);
                }
            } else if (unit.getRarity() == 4) {
                if (isOverflow(unit)) {
                    wishStatus.setTotalStarLight(wishStatus.getTotalStarLight() + 5);
                } else {
                    wishStatus.setTotalStarLight(wishStatus.getTotalStarLight() + 2);
                }
            }

            // 记录日志
            wishResult.add(unit);
            log.info("Wish userId: {}, prob4: {}, prob5: {}, current: {}, result: {}", userId, nextProb4, nextProb5,
                    rand, JSON.toJSONString(unit));
        }

        Dao.addWish(userId, wishType, wishResult);
        wishHistory.addAll(wishResult);
        return wishHistory;
    }

    /**
     * 命之座/精炼是否溢出
     * @param genshinUnit 抽卡对象
     * @return 是否溢出
     */
    public static boolean isOverflow(GenshinUnit genshinUnit) {
        if (genshinUnit.getUnitType() == 1) {
            return genshinUnit.getLevel() > 7;
        } else {
            return genshinUnit.getLevel() > 5;
        }
    }

    /**
     * ID角色映射
     * @param wishHistory 全部祈愿历史
     * @return ID角色映射
     */
    public static List<GenshinUnit> getUnitSummary(List<GenshinUnit> wishHistory) {
        Map<Long, GenshinUnit> unitMap = new HashMap<>(16);
        for (GenshinUnit unit : wishHistory) {
            if (!unitMap.containsKey(unit.getId())) {
                unit.setLevel(1);
                unitMap.put(unit.getId(), unit);
            } else {
                unit.setLevel(unitMap.get(unit.getId()).getLevel() + 1);
                unitMap.put(unit.getId(), unit);
            }
        }
        List<GenshinUnit> result = new ArrayList<>();
        for (Map.Entry<Long, GenshinUnit> entry : unitMap.entrySet()) {
            result.add(entry.getValue());
        }
        return result;
    }

    /**
     * ID数量映射
     * @param wishHistory 全部祈愿历史
     * @return ID数量映射
     */
    private static Map<Long, Integer> getSummary(List<GenshinUnit> wishHistory) {
        Map<Long, Integer> result = new HashMap<>(16);
        for (GenshinUnit unit : wishHistory) {
            if (!result.containsKey(unit.getId())) {
                result.put(unit.getId(), 1);
            } else {
                result.put(unit.getId(), result.get(unit.getId()) + 1);
            }
        }
        return result;
    }

    /**
     * 更新祈愿状态
     * @param wishHistory 祈愿历史
     */
    private static void updateWishStatus(List<GenshinUnit> wishHistory, WishStatus wishStatus) {
        int star4Count = 0;
        int star5Count = 0;
        boolean flag = false;
        for (GenshinUnit genshinUnit : wishHistory) {
            if (genshinUnit.getRarity() < 4) {
                if (!flag) {
                    star4Count ++;
                }
                star5Count ++;
            } else if (genshinUnit.getRarity() == 4) {
                flag = true;
                star5Count ++;
            } else {
                wishStatus.setStar4Count(star4Count);
                wishStatus.setStar5Count(star5Count);
                return;
            }
        }
        wishStatus.setStar4Count(star4Count);
        wishStatus.setStar5Count(star5Count);
    }

    /**
     * 获取4星概率
     * @param wishStatus 祈愿状态
     * @return 4星概率
     */
    private static double getNextProb4(WishStatus wishStatus) {
        return wishStatus.getStar4Count() == 9 ? (1 - getNextProb5(wishStatus)) : 0.051;
    }

    /**
     * 获取5星概率
     * @param wishStatus 祈愿状态
     * @return 5星概率
     */
    private static double getNextProb5(WishStatus wishStatus) {
        if (wishStatus.getStar5Count() <= 72) {
            return 0.006;
        } else {
            return 0.006 + 0.994 * (wishStatus.getStar5Count() - 72) / 17;
        }
    }

    /**
     * 随机选择一个
     * @param available 可选范围
     * @return 选择结果
     */
    private static GenshinUnit chooseOne(List<GenshinUnit> available) {
        return new GenshinUnit(available.get(RAMDOM.nextInt(available.size())));
    }

}
