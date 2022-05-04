package dao;

import com.alibaba.fastjson.JSON;
import entity.service.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

/**
 * @author guohaohao
 */
public class Dao {

    private static final SqlSessionFactory SQL_MAPPER;

    static {
        // 这里打开调试开关
        boolean isDev = false;
        String resources;
        if (isDev) {
            resources = "mybatis-config-dev.xml";
        } else {
            resources = "mybatis-config.xml";
        }

        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(resources);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SQL_MAPPER = new SqlSessionFactoryBuilder().build(reader);
    }

    /**
     * 根据成员相册ID寻找成员
     * @param unitNumber 成员相册ID
     * @return 成员
     */
    public static Unit findUnitByNumber(int unitNumber) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            return session.selectOne("findUnitByNumber", unitNumber);
        }
    }

    /**
     * 根据成员名称寻找成员
     * @param unitName 成员名称
     * @return 成员列表
     */
    public static List<Unit> findUnitByName(String unitName, String unitTag, String name) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("unitName", unitName);
            hashMap.put("unitTag", unitTag);
            hashMap.put("name", name);
            return session.selectList("findUnitByName", hashMap);
        }
    }

    /**
     * 根据群ID搜索关键词
     * @param groupId 群ID
     * @return 关键词列表
     */
    public static List<Keyword> findKeywords(long groupId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            return session.selectList("findKeywords", groupId);
        }
    }

    /**
     * 根据词库ID搜索关键词
     * @param groupId 群ID
     * @param id 词库ID
     * @return 关键词
     */
    public static Keyword findKeywordById(long groupId, long id) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("id", id);
            return session.selectOne("findKeywordById", hashMap);
        }
    }

    /**
     * 根据词库内容搜索关键词
     * @param groupId 群ID
     * @param key 词库内容
     * @param fuzzyMode 模糊查询参数，0为非模糊查询，1为模糊问，2为模糊答
     * @param start 起始位置
     * @return 关键词列表
     */
    public static List<Keyword> findKeywordByKey(long groupId, String key, int fuzzyMode, int start) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("key", key);
            hashMap.put("fuzzyMode", fuzzyMode);
            hashMap.put("start", start);
            return session.selectList("findKeywordByKey", hashMap);
        }
    }

    /**
     * 根据词库内容搜索关键词的数量
     * @param groupId 群ID
     * @param key 词库内容
     * @return 关键词数量
     */
    public static int countKeywordByKey(long groupId, String key, int fuzzyMode) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("fuzzyMode", fuzzyMode);
            hashMap.put("key", key);
            return session.selectOne("countKeywordByKey", hashMap);
        }
    }

    /**
     * 插入关键词
     * @param keyword 关键词
     */
    public static void addKeyword(Keyword keyword) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("addKeyword", keyword);
            session.commit();
        }
    }

    /**
     * 获取当前ID
     * @param groupId 群ID
     * @return 当前ID
     */
    public static Long getId(long groupId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            return session.selectOne("getId", groupId);
        }
    }

    /**
     * 删除关键词
     * @param groupId 群ID
     * @param id 关键词ID
     */
    public static void deleteKeyword(long groupId, long id) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("id", id);
            session.update("deleteKeyword", hashMap);
            session.commit();
        }
    }

    /**
     * 根据日期和ID查找签到情况
     * @param userId 用户ID
     * @param day 日期
     * @return 签到情况
     */
    public static Daily findDailyById(long userId, String day) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("day", day);
            return session.selectOne("findDailyById", hashMap);
        }
    }

    /**
     * 创建一个空的签到状态
     * @param groupId 群ID
     * @param userId 用户ID
     */
    public static void addDaily(long groupId, long userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("userId", userId);
            session.insert("addDaily", hashMap);
            session.commit();
        }
    }

    /**
     * 更新签到状态
     * @param groupId 群ID
     * @param userId 用户ID
     * @param day 日期
     * @param signInResult 签到结果
     */
    public static void updateDailySignIn(long groupId, long userId, String day, int signInResult) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("userId", userId);
            hashMap.put("day", day);
            hashMap.put("signInResult", signInResult);
            session.update("updateDailySignIn", hashMap);
            session.commit();
        }
    }

    /**
     * 更新抽签状态
     * @param groupId 群ID
     * @param userId 用户ID
     * @param day 日期
     * @param drawResult 抽签结果
     */
    public static void updateDailyDraw(long groupId, long userId, String day, int drawResult) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("userId", userId);
            hashMap.put("day", day);
            hashMap.put("drawResult", drawResult);
            session.update("updateDailyDraw", hashMap);
            session.commit();
        }
    }

    /**
     * 更新占卜状态
     * @param groupId 群ID
     * @param userId 用户ID
     * @param day 日期
     * @param divineResult 占卜结果
     */
    public static void updateDailyDivine(long groupId, long userId, String day, int divineResult) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("userId", userId);
            hashMap.put("day", day);
            hashMap.put("divineResult", divineResult);
            session.update("updateDailyDivine", hashMap);
            session.commit();
        }
    }

    /**
     * 查看签到总天数
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 签到总天数
     */
    public static int getSignInDays(long groupId, long userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("userId", userId);
            return session.selectOne("getSignInDays", hashMap);
        }
    }

    /**
     * 创建原石数量
     * @param userId 用户ID
     * @param primogems 原石数量
     */
    public static void createPrimogems(long userId, int primogems, int resin) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("primogems", primogems);
            hashMap.put("resin", resin);
            session.insert("createPrimogems", hashMap);
            session.commit();
        }
    }

    /**
     * 添加原石数量
     * @param userId 用户ID
     * @param primogems 原石数量
     */
    public static void addPrimogems(long userId, int primogems, int starlight, int resin) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("primogems", primogems);
            hashMap.put("starlight", starlight);
            hashMap.put("resin", resin);
            session.update("addPrimogems", hashMap);
            session.commit();
        }
    }

    /**
     * 查询原石数量
     * @param userId 用户ID
     * @return 原石数量
     */
    public static PrimoGems getPrimogems(long userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            return session.selectOne("getPrimogems", hashMap);
        }
    }

    /**
     * 查询抽卡历史
     * @param userId 用户ID
     * @param unitType 类型，0（不区分）/1（角色）/2（武器）
     * @return 抽卡成员列表
     */
    public static List<GenshinUnit> getWishHistoryForSummary(long userId, int unitType) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("unitType", unitType);
            return session.selectList("getWishHistoryForSummary", hashMap);
        }
    }

    /**
     * 查询抽卡历史
     * @param userId 用户ID
     * @param wishEventId 祈愿池子id，1（角色）/2（武器）/3（混合）
     * @param limit 限制数，0为不限
     * @return 抽卡成员列表
     */
    public static List<GenshinUnit> getWishHistory(long userId, int wishEventId, int limit) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("wishEventId", wishEventId);
            hashMap.put("limit", limit);
            return session.selectList("getWishHistory", hashMap);
        }
    }

    /**
     * 获得所有可获得的卡
     * @return 成员列表
     */
    public static List<GenshinUnit> getUnits(int wishEventId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("wishEventId", wishEventId);
            return session.selectList("getUnits", hashMap);
        }
    }

    /**
     * 记录抽卡详情
     * @param userId 用户ID
     * @param wishEventId 祈愿池子id，1（角色）/2（武器）/3（混合）
     * @param wishResult 祈愿结果
     */
    public static void addWish(long userId, int wishEventId, List<GenshinUnit> wishResult) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("wishEventId", wishEventId);
            hashMap.put("wishResult", wishResult);
            session.insert("addWish", hashMap);
            session.commit();
        }
    }

    /**
     * 获得所有祈愿池
     * @return 祈愿池
     */
    public static List<WishEvent> getWishEvents() {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            return session.selectList("getWishEvents", hashMap);
        }
    }

    /**
     * 获得单人祈愿统计
     * @return 祈愿统计
     */
    public static WishSummary getWishSummary(long userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            return session.selectOne("getWishSummary", hashMap);
        }
    }

    /**
     * 获取当前活动信息
     * @return 当前活动信息
     */
    public static SifEvent getCurrentEvent() {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            return session.selectOne("getCurrentEvent");
        }
    }

    /**
     * 获取活动排行榜信息
     * @param eventId 活动ID
     * @return 排行榜信息
     */
    public static List<EventRank> getSifEventRank(int eventId, int secondsAgo) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("eventId", eventId);
            hashMap.put("secondsAgo", secondsAgo);
            return session.selectList("getSifEventRank", hashMap);
        }
    }

    /**
     * 更改祈愿模式
     * @param userId 用户ID
     * @param mode 祈愿模式
     */
    public static void addWishMode(long userId, int mode) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("wishMode", mode);
            session.insert("addWishMode", hashMap);
            session.commit();
        }
    }

    /**
     * 添加原石数量
     * @param userId 用户ID
     * @param mode 祈愿模式
     */
    public static void updateWishMode(long userId, int mode) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("wishMode", mode);
            session.update("updateWishMode", hashMap);
            session.commit();
        }
    }

    /**
     * 查询原石数量
     * @param userId 用户ID
     * @return 原石数量
     */
    public static Integer getWishMode(long userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            return session.selectOne("getWishMode", hashMap);
        }
    }

    /**
     * 获取圣遗物套装
     * @param suitName 圣遗物套装
     * @return 套装信息
     */
    public static List<SaintSuit> getSaintSuit(String suitName) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("suit_name", suitName);
            return session.selectList("getSaintSuit", hashMap);
        }
    }

    /**
     * 保存圣遗物祈愿结果
     * @param saint 圣遗物
     */
    public static void addSaintWish(DbSaint saint) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.insert("addSaintWish", saint);
            session.commit();
        }
    }

    /**
     * 获得圣遗物详情
     * @param id 圣遗物ID
     * @return 圣遗物结果
     */
    public static DbSaint getSaint(int id) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", id);
            return session.selectOne("getSaint", hashMap);
        }
    }

    /**
     * 更新圣遗物祈愿结果
     * @param saint 圣遗物
     */
    public static void updateSaint(DbSaint saint) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            session.update("updateSaint", saint);
            session.commit();
        }
    }

    /**
     * 获取最优圣遗物
     * @param userId 用户ID
     * @param limit 查看数
     * @return 圣遗物结果
     */
    public static List<DbSaint> getBestSaint(long userId, int limit) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("limit", limit);
            return session.selectList("getBestSaint", hashMap);
        }
    }

    /**
     * 获取圣遗物祈愿总数
     * @param userId 用户ID
     * @return 圣遗物结果
     */
    public static int getSaintCount(long userId) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            return session.selectOne("getSaintCount", hashMap);
        }
    }

}
