package utils;

import com.alibaba.fastjson.JSON;
import constant.UnitType;
import dao.Dao;
import entity.service.GenshinUnit;
import entity.service.WishEvent;
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

    private static final Random RANDOM = new Random();
    /**
     * 祈愿类型，1为90抽保底无up池
     */
    private static final int WISH_TYPE_STANDARD = 1;
    /**
     * 祈愿类型，2为80抽保底武器up（75%）池且有大保底
     */
    private static final int WISH_TYPE_WEAPON = 2;
    /**
     * 祈愿类型，3为90抽保底角色up（50%）池且有大保底
     */
    private static final int WISH_TYPE_CHARACTER = 3;

    /**
     * 进行祈愿
     * @param wishCount 祈愿次数，1/10
     * @param wishTypeStr 祈愿名称，不包含“池”
     * @param userId 用户ID
     * @param wishStatus 空的祈愿状态，会被重置
     * @return 祈愿结果
     */
    public static List<GenshinUnit> wish(int wishCount, String wishTypeStr, long userId, WishStatus wishStatus) {

        // 获取所有祈愿池
        WishEvent currentEvent = null;
        List<WishEvent> wishEventList = Dao.getWishEvents();
        for (WishEvent wishEvent : wishEventList) {
            if (wishEvent.getWishEventName().equals(wishTypeStr)) {
                currentEvent = wishEvent;
            }
        }
        if (currentEvent == null) {
            log.info("Wish event name not match!");
            return null;
        }

        // 获取当前祈愿池可选角色
        WishUnitList wishUnitList = getWishUnitList(currentEvent);

        // 获取当前池子祈愿状态，更新保底次数
        List<GenshinUnit> wishHistory = Dao.getWishHistory(userId, currentEvent.getId(), 90);
        updateWishStatus(wishHistory, wishStatus, currentEvent);

        // 获取总祈愿状态，获得命座情况
        List<GenshinUnit> allHistory = Dao.getWishHistoryForSummary(userId, 0);
        Map<Long, Integer> summary = getSummary(allHistory);

        // 祈愿
        List<GenshinUnit> wishResult = new ArrayList<>();
        for (int i = 0; i < wishCount; i ++) {

            // 获取抽卡概率
            double nextProb4 = getNextProb4(wishStatus, currentEvent.getWishType());
            double nextProb5 = getNextProb5(wishStatus, currentEvent.getWishType());
            double rand = RANDOM.nextDouble();
            GenshinUnit unit;

            // 进行抽卡，并更新抽卡状态
            if (rand < nextProb5) {
                // 抽到五星
                unit = chooseOne(5, wishStatus, wishUnitList, currentEvent.getWishType());

                // 更新抽卡状态
                wishStatus.setStar5Count(0);
                wishStatus.setStar4Count(0);
                if (currentEvent.getWishType() == WISH_TYPE_CHARACTER || currentEvent.getWishType() == WISH_TYPE_WEAPON) {
                    wishStatus.setMustUp(unit.getIsUp() != 1);
                }
            } else if (rand < nextProb4 + nextProb5) {
                // 抽到四星
                unit = chooseOne(4, wishStatus, wishUnitList, currentEvent.getWishType());

                // 更新抽卡状态
                wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                wishStatus.setStar4Count(0);
            } else {
                // 抽到三星
                unit = chooseOne(3, wishStatus, wishUnitList, currentEvent.getWishType());

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

        Dao.addWish(userId, currentEvent.getId(), wishResult);
        Collections.reverse(wishHistory);
        wishHistory.addAll(wishResult);
        return wishHistory;
    }

    /**
     * 命之座/精炼是否溢出
     * @param genshinUnit 抽卡对象
     * @return 是否溢出
     */
    public static boolean isOverflow(GenshinUnit genshinUnit) {
        return genshinUnit.getLevel() > UnitType.getById(genshinUnit.getUnitType()).getFullSize();
    }

    /**
     * 命之座/精炼是否满命/满精炼
     * @param genshinUnit 抽卡对象
     * @return 是否溢出
     */
    public static boolean isFull(GenshinUnit genshinUnit) {
        return genshinUnit.getLevel() >= UnitType.getById(genshinUnit.getUnitType()).getFullSize();
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
    private static void updateWishStatus(List<GenshinUnit> wishHistory, WishStatus wishStatus, WishEvent wishEvent) {

        if (wishEvent.getWishType() == WISH_TYPE_WEAPON) {
            wishStatus.setMaxFiveCount(80);
        }

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
                if (wishEvent.getWishType() == WISH_TYPE_CHARACTER || wishEvent.getWishType() == WISH_TYPE_WEAPON) {
                    if (genshinUnit.getIsUp() == 0) {
                        wishStatus.setMustUp(true);
                    }
                }
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
    public static double getNextProb4(WishStatus wishStatus, int wishType) {
        if (wishType == WISH_TYPE_WEAPON) {
            if (wishStatus.getStar4Count() <= 5) {
                return 0.06;
            } else {
                return 0.06 + 0.94 * (wishStatus.getStar4Count() - 5) / 4;
            }
        } else {
            if (wishStatus.getStar4Count() <= 6) {
                return 0.051;
            } else {
                return 0.051 + 0.949 * (wishStatus.getStar4Count() - 6) / 3;
            }
        }
    }

    /**
     * 获取5星概率
     * @param wishStatus 祈愿状态
     * @return 5星概率
     */
    public static double getNextProb5(WishStatus wishStatus, int wishType) {
        if (wishType == WISH_TYPE_WEAPON) {
            if (wishStatus.getStar5Count() <= 62) {
                return 0.007;
            } else {
                return 0.007 + 0.993 * (wishStatus.getStar5Count() - 62) / 17;
            }
        } else {
            if (wishStatus.getStar5Count() <= 72) {
                return 0.006;
            } else {
                return 0.006 + 0.994 * (wishStatus.getStar5Count() - 72) / 17;
            }
        }
    }

    /**
     * 随机选择一个
     * @param rarity 稀有度
     * @return 选择结果
     */
    private static GenshinUnit chooseOne(int rarity, WishStatus wishStatus, WishUnitList wishUnitList, int wishType) {

        // 获取up概率
        double upProb = 0;
        if (wishType == WISH_TYPE_CHARACTER) {
            upProb = 0.5;
        } else if (wishType == WISH_TYPE_WEAPON) {
            upProb = 0.75;
        }

        List<GenshinUnit> available;
        if (rarity == 5) {
            if (wishStatus.isMustUp() || RANDOM.nextDouble() < upProb) {
                available = wishUnitList.getUpStarFiveObject();
            } else {
                available = wishUnitList.getStarFiveObject();
            }
        } else if (rarity == 4) {
            if (RANDOM.nextDouble() < upProb) {
                available = wishUnitList.getUpStarFourObject();
            } else {
                available = wishUnitList.getStarFourObject();
            }
        } else {
            available = wishUnitList.getStarThreeObject();
        }
        return new GenshinUnit(available.get(RANDOM.nextInt(available.size())));
    }

    /**
     * 获取祈愿池对象列表
     * @param wishEvent 祈愿池
     * @return 对象列表
     */
    private static WishUnitList getWishUnitList(WishEvent wishEvent) {

        List<GenshinUnit> starThreeObject = new ArrayList<>();
        List<GenshinUnit> starFourObject = new ArrayList<>();
        List<GenshinUnit> starFiveObject = new ArrayList<>();
        List<GenshinUnit> upStarFiveObject = new ArrayList<>();
        List<GenshinUnit> upStarFourObject = new ArrayList<>();

        List<GenshinUnit> allUnits = Dao.getUnits(wishEvent.getId());
        for (GenshinUnit unit : allUnits) {
            if (unit.getRarity() == 3) {
                starThreeObject.add(unit);
            } else if (unit.getRarity() == 4) {
                if (unit.getIsUp() == 1) {
                    upStarFourObject.add(unit);
                } else {
                    starFourObject.add(unit);
                }
            } else if (unit.getRarity() == 5) {
                if (unit.getIsUp() == 1) {
                    upStarFiveObject.add(unit);
                } else {
                    starFiveObject.add(unit);
                }
            }
        }

        WishUnitList wishUnitList = new WishUnitList();
        wishUnitList.setStarThreeObject(starThreeObject);
        wishUnitList.setStarFourObject(starFourObject);
        wishUnitList.setStarFiveObject(starFiveObject);
        wishUnitList.setUpStarFiveObject(upStarFiveObject);
        wishUnitList.setUpStarFourObject(upStarFourObject);
        log.info("wishUnitList: {}", JSON.toJSONString(wishUnitList));
        return wishUnitList;

    }

    private static class WishUnitList {

        private List<GenshinUnit> starThreeObject = new ArrayList<>();
        private List<GenshinUnit> starFourObject = new ArrayList<>();
        private List<GenshinUnit> starFiveObject = new ArrayList<>();
        private List<GenshinUnit> upStarFiveObject = new ArrayList<>();
        private List<GenshinUnit> upStarFourObject = new ArrayList<>();

        public List<GenshinUnit> getStarThreeObject() {
            return starThreeObject;
        }

        public void setStarThreeObject(List<GenshinUnit> starThreeObject) {
            this.starThreeObject = starThreeObject;
        }

        public List<GenshinUnit> getStarFourObject() {
            return starFourObject;
        }

        public void setStarFourObject(List<GenshinUnit> starFourObject) {
            this.starFourObject = starFourObject;
        }

        public List<GenshinUnit> getStarFiveObject() {
            return starFiveObject;
        }

        public void setStarFiveObject(List<GenshinUnit> starFiveObject) {
            this.starFiveObject = starFiveObject;
        }

        public List<GenshinUnit> getUpStarFiveObject() {
            return upStarFiveObject;
        }

        public void setUpStarFiveObject(List<GenshinUnit> upStarFiveObject) {
            this.upStarFiveObject = upStarFiveObject;
        }

        public List<GenshinUnit> getUpStarFourObject() {
            return upStarFourObject;
        }

        public void setUpStarFourObject(List<GenshinUnit> upStarFourObject) {
            this.upStarFourObject = upStarFourObject;
        }
    }

}
