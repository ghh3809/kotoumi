package dao;

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
     * @param start 起始位置
     * @return 关键词列表
     */
    public static List<Keyword> findKeywordByKey(long groupId, String key, int start) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
            hashMap.put("key", key);
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
    public static int countKeywordByKey(long groupId, String key) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
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
     * @param groupId 群ID
     * @param userId 用户ID
     * @param day 日期
     * @return 签到情况
     */
    public static Daily findDailyById(long groupId, long userId, String day) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupId", groupId);
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
    public static void createPrimogems(long userId, int primogems) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("primogems", primogems);
            session.insert("createPrimogems", hashMap);
            session.commit();
        }
    }

    /**
     * 添加原石数量
     * @param userId 用户ID
     * @param primogems 原石数量
     */
    public static void addPrimogems(long userId, int primogems, int starlight) {
        try (SqlSession session = SQL_MAPPER.openSession()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", userId);
            hashMap.put("primogems", primogems);
            hashMap.put("starlight", starlight);
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
     * @param wishEventId 祈愿池子id，1（角色）/2（武器）/3（混合）
     * @param unitType 类型，0（不区分）/1（角色）/2（武器）
     * @param limit 限制数，0为不限
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

}
