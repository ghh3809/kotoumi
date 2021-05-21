package processor;

import com.alibaba.fastjson.JSON;
import constant.MessageSource;
import constant.MultiTurnTask;
import constant.UnitType;
import dao.Dao;
import entity.service.*;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageContent;
import org.apache.commons.lang3.StringUtils;
import utils.FileHelper;
import utils.RequestHelper;
import utils.WishHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guohaohao
 */
@Slf4j
public class DialogService {

    private static final long ADMIN_QQ = 1146875163L;
    private static final long LIMITED_GROUP = 709375205L;
    /**
     * 图片存储位置
     */
    private static final String CHAT_PIC_DIR = "./pics/chat/";
    private static final String CARD_PIC_DIR = "./pics/card/";
    private static final String DRAW_PIC_DIR = "./pics/draw/";
    private static final String GENSHIN_PIC_DIR = "./pics/genshin/";
    /**
     * 最多备选选项数
     */
    private static final int MAX_CHOICE_NUMBER = 5;
    /**
     * query正则表达式
     */
    private static final Pattern CHOICE_PATTERN = Pattern.compile("^\\d$");
    private static final Pattern SKILL_LEVEL_PATTERN = Pattern.compile("(\\d{1,2})级");
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{1,4}$");
    private static final Pattern KEYWORD_ADD_PATTERN = Pattern.compile("^问[ _](.+?)[ _]答[ _](.+)$", Pattern.DOTALL);
    private static final Pattern KEYWORD_QUERY_PATTERN = Pattern.compile("^(.*?)(_起始_\\d+)?$");
    private static final Pattern WISH_PATTERN = Pattern.compile("^(抽卡|单抽|10连|十连)(.+?)池.*$");
    private static final Pattern ADD_PRIMOGEMS_PATTERN = Pattern.compile("^氪金_(.+?)_(.+)$");
    private static final Pattern SIF_RANK_PATTERN = Pattern.compile("^(国服|当前|实时)?档线$");
    private static final Pattern WISH_RESULT_PATTERN = Pattern.compile("^我的(.+)$");
    /**
     * query关键字
     */
    private static final String HELP_KEYWORD = "帮助";
    private static final String QUERY_CARD_KEYWORD = "查卡";
    private static final String NO_BOX_KEYWORD = "无框";
    private static final String RANK_MAX_KEYWORD = "觉醒";
    private static final String KEYWORD_QUERY_KEYWORD = "查询词库";
    private static final String KEYWORD_DELETE_KEYWORD = "删除词库";
    private static final String SIGN_IN_KEYWORD = "签到";
    private static final String ADD_SIGN_IN_KEYWORD = "补签";
    private static final String DRAW_KEYWORD = "抽签";
    private static final String DIVINE_KEYWORD = "占卜";
    private static final String SUBSTITUTE_KEYWORD = "设置替换关键词";
    private static final String CARD_PK_KEYWORD = "卡组pk";
    private static final String TRANSFORM_KEYWORD = "星辉全部换原石";
    private static final String PROB_KEYWORD = "概率说明";
    private static final String CURRENT_WISH_KEYWORD = "当前卡池";

    /**
     * 回复关键字
     */
    private static final String HELP_RESPONSE = "机器人支持以下模板：\n"
            + "【群聊娱乐】\n"
            + "签到/抽签/占卜\n"
            + "【卡面查询】\n"
            + "查卡：查卡{编号/卡名称}[觉醒][无框]\n"
            + "【词库管理】\n"
            + "新增词库：问_{问题}_答_{答案}\n"
            + "查询词库：查询词库[{词库ID/关键词}][_起始_{起始编号}]\n"
            + "删除词库：删除词库{词库ID/关键词}\n"
            + "【抽卡】\n"
            + "抽卡：抽卡/10连[标准池/角色池/武器池]\n"
            + "查看拥有角色：我的角色\n"
            + "查看拥有武器：我的武器\n"
            + "查看祈愿统计：我的统计\n"
            + "星辉换原石：星辉全部换原石\n"
            + "查看当前开放卡池：当前卡池\n"
            + "获取详细概率说明：概率说明\n"
            + "【关于】\n"
            + "海鸟阁小机器人，请各位善待，如有需求或问题，欢迎随时反馈管理组！";
    private static final String PROB_RESPONSE = "模拟抽卡概率遵循以下原则：\n"
            + "----------\n"
            + "【标准/常驻/角色up池】\n"
            + "4星：默认概率为5.1%，若前7次祈愿未获得4星及以上角色/装备，概率从第8抽开始线性增加，直至第10次时提升至100%\n"
            + "5星：默认概率为0.6%，若前73次祈愿未获得5星角色/装备，概率从第74次开始线性增加，直至第90次时提升至100%\n"
            + "4星综合概率：13.01%，5星综合概率：1.60%\n"
            + "----------\n"
            + "【武器up池】\n"
            + "4星：默认概率为6.0%，若前6次祈愿未获得4星及以上角色/装备，概率从第7抽开始线性增加，直至第10次时提升至100%\n"
            + "5星：默认概率为0.7%，若前63次祈愿未获得5星角色/装备，概率从第64次开始线性增加，直至第80次时提升至100%\n"
            + "4星综合概率：14.29%，5星综合概率：1.85%\n";
    /**
     * 抽签结果列表
     */
    private static final String[] DRAW_TEXT = new String[] {"大凶", "凶", "末吉", "小吉", "吉", "中吉", "中吉", "大吉", "大吉"};
    private static final String[] DIVINE_TEXT = new String[] {
            "单抽出货？不存在的。老老实实肝活动吧",
            "嗯！运气一般呢",
            "你今天运气不错呢",
            "哇！今天欧死了,会有UR吗？",
            "听说你想要的角色限定池快要到了？攒心抽说不定能出货呢！"
    };

    private static final ConcurrentHashMap<String, MultiTurnStatus> MULTI_TURN_STATUS_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, HashMap<String, List<Keyword>>> KEYWORD_MAP = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static boolean IMAGE_FLAG = false;

    public static MessageChain response(MessageChain at, Request request) {
        ResponseFlag responseFlag = new ResponseFlag();
        MessageChain messageChain = response(request, responseFlag);
        if (messageChain != null) {
            if (at != null && responseFlag.needAt) {
                return at.plus(messageChain);
            } else {
                return messageChain;
            }
        } else {
            return null;
        }
    }

    /**
     * 根据用户请求，进行回复
     * @param request 用户请求
     * @return 回复信息
     */
    public static MessageChain response(Request request, ResponseFlag responseFlag) {

        // 预处理
        request.setQuery(request.getQuery().trim());
        String userId = getUserId(request);

        // 当前是否有多轮任务进行中
        if (MULTI_TURN_STATUS_MAP.containsKey(userId)) {
            Matcher choiceMatcher = CHOICE_PATTERN.matcher(request.getQuery());
            if (choiceMatcher.find()) {
                log.info("Choice query found");
                int choice = Integer.parseInt(request.getQuery());
                MultiTurnStatus multiTurnStatus = MULTI_TURN_STATUS_MAP.get(userId);
                if (multiTurnStatus.getMultiTurnTask().equals(MultiTurnTask.QUERY_CARD)) {
                    // 查卡多轮会话
                    if (choice < 1 || choice > multiTurnStatus.getCardNumbers().size()) {
                        return EmptyMessageChain.INSTANCE.plus("请直接回复序号进行选择");
                    } else {
                        return responseCard(multiTurnStatus.getCardNumbers().get(choice - 1),
                                multiTurnStatus.isNoBox(),
                                multiTurnStatus.isRankMax(),
                                multiTurnStatus.getSkillLevel(),
                                request);
                    }
                }
            } else {
                MULTI_TURN_STATUS_MAP.remove(userId);
            }
        }

        // 被限制的群聊：海鸟阁，仅支持档线
        Matcher rankMatcher = SIF_RANK_PATTERN.matcher(request.getQuery());
        if (request.getGroup() != null && request.getGroup().getId() == LIMITED_GROUP) {
            if (rankMatcher.find()) {
                // 国服档线
                return sifRank(request);
            } else {
                return null;
            }
        }

        // 搜索内置关键字
        Matcher addKeywordMatcher = KEYWORD_ADD_PATTERN.matcher(request.getQuery());
        Matcher wishMatcher = WISH_PATTERN.matcher(request.getQuery());
        Matcher addPrimogemsMatcher = ADD_PRIMOGEMS_PATTERN.matcher(request.getQuery());
        Matcher wishResultMatcher = WISH_RESULT_PATTERN.matcher(request.getQuery());
        if (request.getQuery().equals(HELP_KEYWORD)) {
            // 帮助
            return help();
        } else if (request.getQuery().equals(SIGN_IN_KEYWORD)) {
            // 签到
            return signIn(request);
        } else if (request.getQuery().equals(DRAW_KEYWORD)) {
            // 抽签
            return draw(request);
        } else if (request.getQuery().equals(DIVINE_KEYWORD)) {
            // 占卜
            return divine(request);
        } else if (request.getQuery().startsWith(QUERY_CARD_KEYWORD)) {
            // 查卡
            return queryCard(request);
        } else if (request.getQuery().startsWith(KEYWORD_QUERY_KEYWORD)) {
            // 查询词库
            return queryKeyword(request);
        } else if (request.getQuery().startsWith(KEYWORD_DELETE_KEYWORD)) {
            // 删除词库
            return deleteKeyword(request);
        } else if (request.getQuery().startsWith(SUBSTITUTE_KEYWORD)) {
            // 设置替换关键词
            return substitute(request);
        } else if (wishMatcher.find()) {
            // 抽卡
            return wish(request, wishMatcher);
        } else if (request.getQuery().startsWith(CARD_PK_KEYWORD)) {
            // 卡组pk
            return cardPk(request);
        } else if (request.getQuery().startsWith(ADD_SIGN_IN_KEYWORD)) {
            // 签到
            return addSignIn(request);
        } else if (addKeywordMatcher.find()) {
            // 添加词库
            return addKeyword(request, addKeywordMatcher);
        } else if (wishResultMatcher.find()) {
            // 我的祈愿信息
            return myWish(request, wishResultMatcher);
        } else if (request.getQuery().equals(TRANSFORM_KEYWORD)) {
            // 星辉全部换原石
            return transform(request);
        } else if (request.getQuery().equals(PROB_KEYWORD)) {
            // 概率说明
            return prob(request);
        } else if (addPrimogemsMatcher.find()) {
            // 增加原石
            return addPrimogems(request, addPrimogemsMatcher);
        } else if (request.getQuery().equals(CURRENT_WISH_KEYWORD)) {
            // 当前卡池
            return currentWish(request);
        } else if (rankMatcher.find()) {
            // 国服档线
            return sifRank(request);
        }

        // 群自定义词库
        if (request.getMessageSource().equals(MessageSource.GROUP)) {
            if (!KEYWORD_MAP.containsKey(request.getGroup().getId())) {
                KEYWORD_MAP.put(request.getGroup().getId(), buildKeywordMap(request.getGroup().getId()));
            }
            List<Keyword> keywordList = KEYWORD_MAP.get(request.getGroup().getId()).get(request.getQuery());
            if (keywordList != null && !keywordList.isEmpty()) {
                Keyword keyword = keywordList.get(RANDOM.nextInt(keywordList.size()));
                log.info("Keyword query found: {}", keyword.getId());
                responseFlag.needAt = false;
                return responsePattern(keyword.getResponse(), request);
            }
        }

        // 均不满足时的回复
        return defaultResponse(request);
    }

    /**
     * 帮助菜单
     * @return 返回
     */
    private static MessageChain help() {
        log.info("Help found");
        return EmptyMessageChain.INSTANCE.plus(HELP_RESPONSE);
    }

    /**
     * 签到
     * @param request 请求
     * @return 返回
     */
    private static MessageChain signIn(Request request) {

        log.info("Sign in found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        // 获取当前daily
        String day = SIMPLE_DATE_FORMAT.format(new Date());
        Daily daily = Dao.findDailyById(request.getGroup().getId(), request.getFrom(), day);
        if (daily == null) {
            Dao.addDaily(request.getGroup().getId(), request.getFrom());
            daily = Dao.findDailyById(request.getGroup().getId(), request.getFrom(), day);
        }

        // 签到
        if (daily.getSignInResult() == 0) {
            Dao.updateDailySignIn(request.getGroup().getId(), request.getFrom(), day, 1);
            // 获得原石数
            int signInDays = Dao.getSignInDays(request.getGroup().getId(), request.getFrom());
            int addPrimogems = 300 + signInDays / 10 * 50;
            if (addPrimogems > 600) {
                addPrimogems = 600;
            }

            // 签到前缀
            if (signInDays == 1) {
                addPrimogems = 20000;
                Dao.createPrimogems(request.getFrom(), addPrimogems);
            } else {
                Dao.addPrimogems(request.getFrom(), addPrimogems, 0);
            }
            int primogems = 0;
            PrimoGems primoGemsInfo = Dao.getPrimogems(request.getFrom());
            if (primoGemsInfo != null) {
                primogems = primoGemsInfo.getPrimogems();
            }

            return EmptyMessageChain.INSTANCE.plus("\n签到成功！\n累计签到" + signInDays + "天\n获得原石"
                    + addPrimogems + "个\n剩余原石" + primogems + "个");
        } else {
            return EmptyMessageChain.INSTANCE.plus("你今天已经签到过了，请明天再来~~");
        }
    }

    /**
     * 补签
     * @param request 请求
     * @return 返回
     */
    private static MessageChain addSignIn(Request request) {
        log.info("Add sign in found");
        return EmptyMessageChain.INSTANCE.plus("mabo暂时还不支持补签哦~");
    }

    /**
     * 抽签
     * @param request 请求
     * @return 返回
     */
    private static MessageChain draw(Request request) {

        log.info("Draw found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        // 获取当前daily
        String day = SIMPLE_DATE_FORMAT.format(new Date());
        Daily daily = Dao.findDailyById(request.getGroup().getId(), request.getFrom(), day);
        if (daily == null) {
            Dao.addDaily(request.getGroup().getId(), request.getFrom());
            daily = Dao.findDailyById(request.getGroup().getId(), request.getFrom(), day);
        }

        // 抽签
        if (daily.getDrawResult() == 0) {
            int drawResult = RANDOM.nextInt(DRAW_TEXT.length) + 1;
            Dao.updateDailyDraw(request.getGroup().getId(), request.getFrom(), day, drawResult);
            return EmptyMessageChain.INSTANCE.plus("抽签成功！\n你今天的运势为：\n")
                    .plus(uploadImage(request, new File(DRAW_PIC_DIR + drawResult + ".jpg")));
        } else {
            return EmptyMessageChain.INSTANCE.plus("你今天已经抽签过了，请明天再来~~\n你今天的运势为：" + DRAW_TEXT[daily.getDrawResult() - 1]);
        }
    }

    /**
     * 占卜
     * @param request 请求
     * @return 返回
     */
    private static MessageChain divine(Request request) {

        log.info("Divine found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        // 获取当前daily
        String day = SIMPLE_DATE_FORMAT.format(new Date());
        Daily daily = Dao.findDailyById(request.getGroup().getId(), request.getFrom(), day);
        if (daily == null) {
            Dao.addDaily(request.getGroup().getId(), request.getFrom());
            daily = Dao.findDailyById(request.getGroup().getId(), request.getFrom(), day);
        }

        // 占卜
        if (daily.getDivineResult() == 0) {
            int divineResult = RANDOM.nextInt(DIVINE_TEXT.length) + 1;
            Dao.updateDailyDivine(request.getGroup().getId(), request.getFrom(), day, divineResult);
            return EmptyMessageChain.INSTANCE.plus(DIVINE_TEXT[divineResult - 1]);
        } else {
            return EmptyMessageChain.INSTANCE.plus("你今天已经占卜过了，请明天再来~~\n你今天的占卜结果是：\n" + DIVINE_TEXT[daily.getDivineResult() - 1]);
        }
    }

    /**
     * 设置替换关键词
     * @param request 请求
     * @return 返回
     */
    private static MessageChain substitute(Request request) {
        log.info("Substitute found");
        return EmptyMessageChain.INSTANCE.plus("mabo暂时还不支持关键词替换哦~");
    }

    /**
     * 抽卡
     * @param request 请求
     * @return 返回
     */
    private static MessageChain wish(Request request, Matcher matcher) {

        log.info("Wish found");
        // 预处理
        String wishCountStr = matcher.group(1);
        int wishCount = 10;
        if ("抽卡".equals(wishCountStr) || "单抽".equals(wishCountStr)) {
            wishCount = 1;
        }
        String wishTypeStr = matcher.group(2);

        // 校验原石数量
        PrimoGems primoGemsInfo = Dao.getPrimogems(request.getFrom());
        int primogems = 0;
        if (primoGemsInfo != null) {
            primogems = primoGemsInfo.getPrimogems();
        }
        if (primogems < wishCount * 160) {
            return EmptyMessageChain.INSTANCE.plus("\n抽卡失败，原石不足！\n当前原石数：" + primogems + "个");
        }

        // 抽卡与结果转义
        WishStatus wishStatus = new WishStatus();
        List<GenshinUnit> result = WishHelper.wish(wishCount, wishTypeStr, request.getFrom(), wishStatus);
        if (result == null) {
            return EmptyMessageChain.INSTANCE.plus("\n抽卡失败，当前卡池不存在！");
        }
        Dao.addPrimogems(request.getFrom(), - wishCount * 160, wishStatus.getTotalStarLight());
        StringBuilder stringBuilder = new StringBuilder("\n抽卡成功！花费原石：")
                .append(wishCount * 160)
                .append("个，剩余原石：")
                .append(primogems - wishCount * 160)
                .append("个\n抽卡结果：\n");
        List<Long> picUnits = new ArrayList<>();

        for (int i = 0; i < wishCount; i ++) {

            // 生成命座/精炼信息
            GenshinUnit genshinUnit = result.get(result.size() - (wishCount - i));
            UnitType unitType = UnitType.getById(genshinUnit.getUnitType());
            String levelInfo = "";
            if (genshinUnit.getRarity() > 3) {
                levelInfo = generateLevelInfo(genshinUnit);
                picUnits.add(genshinUnit.getId());
            }

            // 生成抽卡结果信息
            stringBuilder.append(i + 1).append(". ");
            for (int j = 0; j < genshinUnit.getRarity(); j ++) {
                stringBuilder.append("★");
            }
            stringBuilder.append(" ")
                    .append(unitType.getTypeName())
                    .append(" ")
                    .append(genshinUnit.getUnitName())
                    .append(levelInfo)
                    .append("\n");
        }
        MessageChain messageChain = EmptyMessageChain.INSTANCE.plus(stringBuilder);

        // 图片展示
        for (Long id : picUnits) {
            File file = new File(GENSHIN_PIC_DIR + id + ".png");
            if (file.exists()) {
                messageChain = messageChain.plus(uploadImage(request, file)).plus("\n");
            }
        }

        // 保底统计
        stringBuilder = new StringBuilder("\n");
        stringBuilder.append("获得星辉数：").append(wishStatus.getTotalStarLight()).append("\n");
        stringBuilder.append("距离下次4★保底还剩：").append(10 - wishStatus.getStar4Count()).append("抽\n");
        stringBuilder.append("距离下次5★保底还剩：").append(wishStatus.getMaxFiveCount() - wishStatus.getStar5Count()).append("抽");

        return messageChain.plus(stringBuilder);
    }

    /**
     * 卡组pk
     * @param request 请求
     * @return 返回
     */
    private static MessageChain cardPk(Request request) {
        log.info("Card pk found");
        return EmptyMessageChain.INSTANCE.plus("mabo暂时还不支持卡组pk哦~");
    }

    /**
     * 查看自己的祈愿信息
     * @param request 请求信息
     * @param matcher 正则
     * @return 返回
     */
    private static MessageChain myWish(Request request, Matcher matcher) {

        log.info("My wish found");
        String keyword = matcher.group(1);
        if ("统计".equals(keyword)) {
            return mySummary(request);
        } else {
            UnitType unitType = UnitType.getByName(keyword);
            if (unitType != null) {

                List<GenshinUnit> wishHistory = Dao.getWishHistoryForSummary(request.getFrom(), unitType.getId());
                List<GenshinUnit> wishSummary = WishHelper.getUnitSummary(wishHistory);

                // 拼接字符串
                StringBuilder stringBuilder5 = new StringBuilder("\n获得的五星" + unitType.getTypeName() + "：\n");
                StringBuilder stringBuilder4 = new StringBuilder("\n获得的四星" + unitType.getTypeName() + "：\n");
                int star5Count = 0;
                int star4Count = 0;
                boolean star5Flag = false;
                boolean star4Flag = false;
                for (GenshinUnit unit : wishSummary) {
                    String levelInfo = generateLevelInfo(unit);
                    if (unit.getRarity() == 5) {
                        stringBuilder5.append(++ star5Count)
                                .append(". ")
                                .append(unit.getUnitName())
                                .append(levelInfo)
                                .append("\n");
                        star5Flag = true;
                    } else if (unit.getRarity() == 4) {
                        stringBuilder4.append(++ star4Count)
                                .append(". ")
                                .append(unit.getUnitName())
                                .append(levelInfo)
                                .append("\n");
                        star4Flag = true;
                    }
                }
                if (!star5Flag) {
                    stringBuilder5.append("暂无\n");
                }
                if (!star4Flag) {
                    stringBuilder4.append("暂无\n");
                }
                stringBuilder4.deleteCharAt(stringBuilder4.length() - 1);
                MessageChain messageChain = EmptyMessageChain.INSTANCE;
                messageChain = messageChain.plus(stringBuilder5);
                messageChain = messageChain.plus(stringBuilder4);

                return messageChain;
            }
        }
        return null;
    }

    /**
     * 星辉换原石
     * @param request 请求
     * @return 返回
     */
    private static MessageChain transform(Request request) {
        log.info("Transform found");
        PrimoGems primoGems = Dao.getPrimogems(request.getFrom());
        log.info("Current status: {}", JSON.toJSONString(primoGems));
        if (primoGems.getStarlight() < 5) {
            return EmptyMessageChain.INSTANCE.plus("星辉数量不足，当前星辉数量：" + primoGems.getStarlight());
        }
        int times = primoGems.getStarlight() / 5;
        Dao.addPrimogems(request.getFrom(), times * 160, - times * 5);
        return EmptyMessageChain.INSTANCE.plus("兑换成功！当前星辉数量：" + (primoGems.getStarlight() - times * 5)
                + "，当前原石数量：" + (primoGems.getPrimogems() + times * 160));
    }

    /**
     * 星辉换原石
     * @param request 请求
     * @return 返回
     */
    private static MessageChain prob(Request request) {
        log.info("Prob found");
        return EmptyMessageChain.INSTANCE.plus(PROB_RESPONSE);
    }

    /**
     * 星辉换原石
     * @param request 请求
     * @return 返回
     */
    private static MessageChain addPrimogems(Request request, Matcher matcher) {
        log.info("Add Primogems found");
        if (request.getFrom() != ADMIN_QQ) {
            return EmptyMessageChain.INSTANCE.plus("无权访问！");
        }
        long userId = Long.parseLong(matcher.group(1));
        int amount = Integer.parseInt(matcher.group(2));
        Dao.addPrimogems(userId, amount, 0);
        int current = Dao.getPrimogems(userId).getPrimogems();
        return EmptyMessageChain.INSTANCE.plus("氪金成功！账号：" + userId + ", 添加原石数：" + amount
                + ", 剩余原石数：" + current);
    }

    /**
     * 查看当前开放的卡池
     * @param request 请求
     * @return 返回
     */
    private static MessageChain currentWish(Request request) {
        log.info("Current Wish found");
        List<WishEvent> wishEventList = Dao.getWishEvents();
        StringBuilder stringBuilder = new StringBuilder("当前开放的卡池：\n");
        for (WishEvent wishEvent : wishEventList) {
            stringBuilder.append("【")
                    .append(wishEvent.getWishEventName())
                    .append("池】\n五星范围：")
                    .append(wishEvent.getUnitFiveRegion())
                    .append("\n四星范围：")
                    .append(wishEvent.getUnitFourRegion())
                    .append("\n结束时间：")
                    .append(wishEvent.getEndTime())
                    .append("\n");
        }
        stringBuilder.append("请直接输入“抽卡/10连xx池”进行抽卡");
        return EmptyMessageChain.INSTANCE.plus(stringBuilder);
    }

    /**
     * 查看祈愿统计
     * @param request 请求
     * @return 返回
     */
    private static MessageChain mySummary(Request request) {
        log.info("My Summary found");
        WishSummary wishSummary = Dao.getWishSummary(request.getFrom());
        StringBuilder stringBuilder = new StringBuilder("\n当前总抽卡次数：")
                .append(wishSummary.getTotalCount())
                .append("\n抽到的五星数：")
                .append(wishSummary.getStarFiveCount())
                .append("，其中角色")
                .append(wishSummary.getStarFiveCharacterCount())
                .append("个，武器")
                .append(wishSummary.getStarFiveWeaponCount())
                .append("个\n五星出货率：")
                .append(String.format("%.2f", wishSummary.getStarFiveCount() * 100.0 / wishSummary.getTotalCount()))
                .append("%\n抽到的四星数：")
                .append(wishSummary.getStarFourCount())
                .append("，其中角色")
                .append(wishSummary.getStarFourCharacterCount())
                .append("个，武器")
                .append(wishSummary.getStarFourWeaponCount())
                .append("个\n四星出货率：")
                .append(String.format("%.2f", wishSummary.getStarFourCount() * 100.0 / wishSummary.getTotalCount()))
                .append("%");
        return EmptyMessageChain.INSTANCE.plus(stringBuilder);
    }

    /**
     * 查看sif国服档线
     * @param request 请求
     * @return 返回
     */
    private static MessageChain sifRank(Request request) {
        log.info("SIF rank found");
        SifEvent sifEvent = Dao.getCurrentEvent();
        log.info("sifEvent: {}", sifEvent);
        if (sifEvent == null) {
            return EmptyMessageChain.INSTANCE.plus("未查询到当前活动");
        }
        StringBuilder stringBuilder = new StringBuilder("\n----------\n【活动信息】\n当前活动: ");
        stringBuilder.append(sifEvent.getEventName())
                .append("\n活动时间: ")
                .append(sifEvent.getStartTime())
                .append(" ~ ")
                .append(sifEvent.getEndTime())
                .append("\n活动状态: ")
                .append(sifEvent.getActive() ? "进行中" : "已结束")
                .append("\n----------\n【PT榜信息】\n");

        List<EventRank> eventRankList = Dao.getSifEventRank(sifEvent.getEventId(), 0);
        List<EventRank> eventRankListLastHour = Dao.getSifEventRank(sifEvent.getEventId(), 3600);
        log.info("eventRankList: {}", eventRankList);
        int pt1 = -1;
        int changePt1 = 0;
        String timePt1 = "";
        int pt2 = -1;
        int changePt2 = 0;
        String timePt2 = "";
        int pt3 = -1;
        int changePt3 = 0;
        String timePt3 = "";
        int live1 = -1;
        int changeLive1 = 0;
        String timeLive1 = "";
        int live2 = -1;
        int changeLive2 = 0;
        String timeLive2 = "";
        for (EventRank eventRank : eventRankList) {
            if ("pt".equals(eventRank.getType())) {
                if (eventRank.getRank() == 120) {
                    pt1 = eventRank.getScore();
                    timePt1 = eventRank.getRequestTime().substring(5, 16);
                } else if (eventRank.getRank() == 700) {
                    pt2 = eventRank.getScore();
                    timePt2 = eventRank.getRequestTime().substring(5, 16);
                } else if (eventRank.getRank() == 2300) {
                    pt3 = eventRank.getScore();
                    timePt3 = eventRank.getRequestTime().substring(5, 16);
                }
            } else {
                if (eventRank.getRank() == 2300) {
                    live1 = eventRank.getScore();
                    timeLive1 = eventRank.getRequestTime().substring(5, 16);
                } else if (eventRank.getRank() == 6900) {
                    live2 = eventRank.getScore();
                    timeLive2 = eventRank.getRequestTime().substring(5, 16);
                }
            }
        }
        for (EventRank eventRank : eventRankListLastHour) {
            if ("pt".equals(eventRank.getType())) {
                if (eventRank.getRank() == 120) {
                    changePt1 = pt1 - eventRank.getScore();
                } else if (eventRank.getRank() == 700) {
                    changePt2 = pt2 - eventRank.getScore();
                } else if (eventRank.getRank() == 2300) {
                    changePt3 = pt3 - eventRank.getScore();
                }
            } else {
                if (eventRank.getRank() == 2300) {
                    changeLive1 = live1 - eventRank.getScore();
                } else if (eventRank.getRank() == 6900) {
                    changeLive2 = live2 - eventRank.getScore();
                }
            }
        }

        stringBuilder.append("PT一档(Rk. 120): \n")
                .append(pt1 > 0 ? (pt1 + " (" + timePt1 + ", +" + changePt1 + "/h)") : "未知")
                .append("\n")
                .append("PT二档(Rk. 700): \n")
                .append(pt2 > 0 ? (pt2 + " (" + timePt2 + ", +" + changePt2 + "/h)") : "未知")
                .append("\n")
                .append("PT三档(Rk. 2300): \n")
                .append(pt3 > 0 ? (pt3 + " (" + timePt3 + ", +" + changePt3 + "/h)") : "未知")
                .append("\n----------\n【歌榜信息】\n")
                .append("歌榜一档(Rk. 2300): \n")
                .append(live1 > 0 ? (live1 + " (" + timeLive1 + ", +" + changeLive1 + "/h)") : "未知")
                .append("\n")
                .append("歌榜二档(Rk. 6900): \n")
                .append(live2 > 0 ? (live2 + " (" + timeLive2 + ", +" + changeLive2 + "/h)") : "未知")
                .append("\n----------\n若发现档线更新不及时，可以接入海鸟站（http://kotoumi.top）并在游戏中查看对应档线帮我恢复哦~");
        return EmptyMessageChain.INSTANCE.plus(stringBuilder);

    }

    /**
     * 回复查卡（目前只支持相册编号查卡）
     * @param request 用户请求
     * @return 返回
     */
    private static MessageChain queryCard(Request request) {

        log.info("Card query found");

        // 删除查卡关键字
        String cardQuery = request.getQuery().replaceFirst(QUERY_CARD_KEYWORD, "");

        // 更新无框关键字
        boolean noBox = false;
        if (cardQuery.contains(NO_BOX_KEYWORD)) {
            noBox = true;
            cardQuery = cardQuery.replaceFirst(NO_BOX_KEYWORD, "");
        }

        // 更新觉醒关键字
        boolean rankMax = false;
        if (cardQuery.contains(RANK_MAX_KEYWORD)) {
            rankMax = true;
            cardQuery = cardQuery.replaceFirst(RANK_MAX_KEYWORD, "");
        }

        // 更新等级关键字
        int skillLevel = 1;
        Matcher matcher = SKILL_LEVEL_PATTERN.matcher(cardQuery);
        if (matcher.find()) {
            skillLevel = Integer.parseInt(matcher.group(1));
            cardQuery = cardQuery.replaceFirst(matcher.group(1) + "级", "");
        }

        cardQuery = cardQuery.trim();
        log.info("Actual card query: {}", cardQuery);
        if (StringUtils.isBlank(cardQuery)) {
            return EmptyMessageChain.INSTANCE.plus("请使用指令：“查卡{编号/卡名称}[觉醒][无框]”");
        }

        // 查卡结果
        if (CARD_NUMBER_PATTERN.matcher(cardQuery).find()) {
            int cardNumber = Integer.parseInt(cardQuery);
            return responseCard(cardNumber, noBox, rankMax, skillLevel, request);
        } else {
            UnitTag unitTag = extractUnitName(cardQuery);
            List<Unit> unitList;
            if (unitTag.name != null) {
                log.info("Tag: {}, Name: {}", unitTag.tag, unitTag.name);
                unitList = Dao.findUnitByName(cardQuery, unitTag.tag, unitTag.name);
            } else {
                unitList = Dao.findUnitByName(cardQuery, cardQuery, null);
            }

            if (!unitList.isEmpty()) {
                if (unitList.size() == 1) {
                    return responseCard(unitList.get(0).getUnitNumber(), noBox, rankMax, skillLevel, request);
                } else {
                    // 多结果时的多轮交互准备
                    MultiTurnStatus multiTurnStatus = new MultiTurnStatus();
                    multiTurnStatus.setMultiTurnTask(MultiTurnTask.QUERY_CARD);
                    multiTurnStatus.setCardNumbers(new ArrayList<>());
                    multiTurnStatus.setNoBox(noBox);
                    multiTurnStatus.setRankMax(rankMax);
                    multiTurnStatus.setSkillLevel(skillLevel);

                    // 遍历所有搜索结果
                    StringBuilder stringBuilder = new StringBuilder("查询到以下结果：\n");
                    for (int i = 0; i < Math.min(5, unitList.size()); i ++) {
                        Unit unit = unitList.get(i);
                        stringBuilder.append(i + 1)
                                .append(". ")
                                .append(unit.getUnitNumber())
                                .append(" ")
                                .append(unit.getName())
                                .append("(")
                                .append(unit.getEponym())
                                .append(")\n");
                        multiTurnStatus.getCardNumbers().add(unit.getUnitNumber());
                    }
                    if (unitList.size() > MAX_CHOICE_NUMBER) {
                        stringBuilder.append("（查询结果大于5条，更多结果不进行展示）");
                    }
                    stringBuilder.append("请直接回复序号进行选择");

                    // 保存多轮状态
                    MULTI_TURN_STATUS_MAP.put(getUserId(request), multiTurnStatus);
                    return EmptyMessageChain.INSTANCE.plus(stringBuilder.toString());
                }
            } else {
                return EmptyMessageChain.INSTANCE.plus("无查询结果，请使用指令：“查卡{编号/卡名称}[觉醒][无框]”");
            }
        }
    }

    /**
     * 用户添加词库
     * @param request 用户请求
     * @return 返回
     */
    private static MessageChain addKeyword(Request request, Matcher matcher) {

        log.info("Add keyword found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        // 存储图片
        String key = matcher.group(1);
        String value = matcher.group(2);
        log.info("key: {}, value: {}", key, value);
        if (!saveImage(request, key) || !saveImage(request, value)) {
            return null;
        }

        // 更新关键词
        Keyword keyword = new Keyword();
        keyword.setGroupId(request.getGroup().getId());
        keyword.setCreatorId(request.getFrom());
        keyword.setKeyword(key.trim());
        keyword.setResponse(value.trim());
        Dao.addKeyword(keyword);
        long id = Dao.getId(request.getGroup().getId());
        KEYWORD_MAP.put(request.getGroup().getId(), buildKeywordMap(request.getGroup().getId()));
        return EmptyMessageChain.INSTANCE.plus("学到新知识了呢(ID: " + id + ")~不信你回复“")
                .plus(responsePattern(key, request))
                .plus("”试试~");
    }

    /**
     * 查询词库
     * @param request 请求
     * @return 返回
     */
    private static MessageChain queryKeyword(Request request) {

        log.info("Query keyword found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        request.setQuery(request.getQuery().replaceFirst(KEYWORD_QUERY_KEYWORD, ""));
        Matcher matcher = KEYWORD_QUERY_PATTERN.matcher(request.getQuery());
        if (matcher.find()) {
            String query = matcher.group(1);
            String start = matcher.group(2);

            // 提取开始序号
            int startIndex = 1;
            if (start != null) {
                startIndex = Integer.parseInt(start.substring("_起始_".length()));
            }
            log.info("Actual query: {}, start: {}", query, startIndex);

            // 判断是序号还是关键词
            if (CARD_NUMBER_PATTERN.matcher(query).find()) {
                // 说明是按序号进行查看
                int keywordId = Integer.parseInt(query);
                Keyword keyword = Dao.findKeywordById(request.getGroup().getId(), keywordId);
                if (keyword == null) {
                    return EmptyMessageChain.INSTANCE.plus("词库不存在，请使用指令：“查询词库[{词库ID/关键词}][_起始_{起始编号}]”");
                } else {
                    return EmptyMessageChain.INSTANCE.plus("查询到以下结果：")
                            .plus(responseKeywords(Collections.singletonList(keyword), request));
                }
            } else {
                // 说明是按关键词进行查看
                List<Keyword> keywordList = Dao.findKeywordByKey(request.getGroup().getId(), query, startIndex - 1);
                if (keywordList.isEmpty()) {
                    return EmptyMessageChain.INSTANCE.plus("词库不存在，请使用指令：“查询词库[{词库ID/关键词}][_起始_{起始编号}]”");
                } else {
                    int totalCount = Dao.countKeywordByKey(request.getGroup().getId(), query);
                    return EmptyMessageChain.INSTANCE.plus("查询到以下结果：")
                            .plus(responseKeywords(keywordList, request))
                            .plus("\n----------\n当前显示第" + startIndex + "-" + (startIndex + keywordList.size() - 1) + "条，共" + totalCount + "条");
                }
            }
        } else {
            return EmptyMessageChain.INSTANCE.plus("请使用指令：“查询词库[{词库ID/关键词}][_起始_{起始编号}]”");
        }

    }

    private static MessageChain deleteKeyword(Request request) {

        log.info("Delete keyword found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        String query = request.getQuery().replaceFirst(KEYWORD_DELETE_KEYWORD, "");
        log.info("Actual query: {}", request.getQuery());
        // 判断是序号还是关键词
        if (CARD_NUMBER_PATTERN.matcher(query).find()) {
            // 说明是按序号进行删除
            int keywordId = Integer.parseInt(query);
            Keyword keyword = Dao.findKeywordById(request.getGroup().getId(), keywordId);
            if (keyword == null) {
                return EmptyMessageChain.INSTANCE.plus("词库不存在，请使用指令：“删除词库{词库ID/关键词}”");
            } else {
                if (request.getFrom() == keyword.getCreatorId() ||
                        request.getGroup().get(request.getFrom()).getPermission().compareTo(MemberPermission.ADMINISTRATOR) >= 0) {
                    Dao.deleteKeyword(request.getGroup().getId(), keywordId);
                    KEYWORD_MAP.put(request.getGroup().getId(), buildKeywordMap(request.getGroup().getId()));
                    return EmptyMessageChain.INSTANCE.plus("已删除：")
                            .plus(responseKeywords(Collections.singletonList(keyword), request));
                } else {
                    return EmptyMessageChain.INSTANCE.plus("无权删除，请联系词库创建者(" + keyword.getCreatorId() + ")或管理员删除");
                }
            }
        } else {
            // 说明是按关键词进行查看
            List<Keyword> keywordList = Dao.findKeywordByKey(request.getGroup().getId(), query, 0);
            if (keywordList.isEmpty()) {
                return EmptyMessageChain.INSTANCE.plus("词库不存在，请使用指令：“删除词库{词库ID/关键词}”");
            } else {
                if (keywordList.size() == 1) {
                    Keyword keyword = keywordList.get(0);
                    if (request.getFrom() == keyword.getCreatorId() ||
                            request.getGroup().get(request.getFrom()).getPermission().compareTo(MemberPermission.ADMINISTRATOR) >= 0) {
                        Dao.deleteKeyword(request.getGroup().getId(), keyword.getId());
                        KEYWORD_MAP.put(request.getGroup().getId(), buildKeywordMap(request.getGroup().getId()));
                        return EmptyMessageChain.INSTANCE.plus("已删除：")
                                .plus(responseKeywords(Collections.singletonList(keyword), request));
                    } else {
                        return EmptyMessageChain.INSTANCE.plus("无权删除，请联系词库创建者(" + keyword.getCreatorId() + ")或管理员删除");
                    }
                } else {
                    int totalCount = Dao.countKeywordByKey(request.getGroup().getId(), query);
                    return EmptyMessageChain.INSTANCE.plus("查询到以下结果：")
                            .plus(responseKeywords(keywordList, request))
                            .plus("\n----------\n当前显示第1-" + keywordList.size() + "条，共" + totalCount + "条。请直接回复“删除词库{词库ID}”进行删除");
                }
            }
        }
    }

    /**
     * 默认回复
     * @param request 用户请求
     * @return 返回
     */
    private static MessageChain defaultResponse(Request request) {
        if (!request.getMessageSource().equals(MessageSource.GROUP) || request.getQuerySignal().isAtMe()) {
            String response = UnitService.dialog(getUserId(request), request.getQuery());
            if (response != null) {
                return EmptyMessageChain.INSTANCE.plus(response);
            } else {
                return EmptyMessageChain.INSTANCE.plus("这个问题我现在还不会，你可以用\"问_{问题}_答_{答案}\"来教我哦！");
            }
        }
        return null;
    }

    /**
     * 根据卡的相册ID，获取卡面
     * @param cardNumber 卡相册ID
     * @param noBox 是否无框
     * @param rankMax 是否觉醒
     * @param request 请求
     * @return 响应
     */
    private static MessageChain responseCard(int cardNumber, boolean noBox, boolean rankMax, int skillLevel, Request request) {

        // 查询卡面
        File file = new File(CARD_PIC_DIR + cardNumber + (noBox ? "-nb" : "") + (rankMax ? "-mr" : "") + ".png");
        if (!file.exists()) {
            Unit unit = Dao.findUnitByNumber(cardNumber);
            String unitUrl = String.format("https://card.niconi.co.ni/card/v4%s/%d.png",
                    noBox ? "nb" : "",
                    rankMax ? unit.getRankMaxCardId() : unit.getNormalCardId());
            file = FileHelper.download(unitUrl, file);
            if (file == null) {
                return null;
            }
        }

        // 查询技能
        String skillDescription = null;
        try {
            File fileData = new File(CARD_PIC_DIR + cardNumber + ".txt");
            if (!fileData.exists()) {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("cookie", "uiLocalize=zh-cn; dbLocalize=CN");
                String response = RequestHelper.httpGet(String.format("https://card.niconi.co.ni/cardApi/%d", cardNumber), headers);
                FileHelper.saveToFile(fileData, Collections.singletonList(response), false);
            }
            String result = Objects.requireNonNull(FileHelper.readLines(fileData)).get(0).trim();
            skillDescription = JSON.parseObject(result).getJSONArray("skill_level").getJSONObject(skillLevel - 1).getString("description");
        } catch (Exception e) {
            log.error("Get skill description error: {}", e.fillInStackTrace().toString());
        }

        MessageChain messageChain = EmptyMessageChain.INSTANCE.plus(uploadImage(request, file));
        messageChain = messageChain.plus("\nID：" + cardNumber);
        if (skillDescription != null) {
            messageChain = messageChain.plus("\n技能(" + skillLevel + "级)：" + skillDescription);
        }
        return messageChain;
    }

    /**
     * 响应keyword模板格式
     * @param response 响应模板
     * @return 返回
     */
    private static MessageChain responsePattern(String response, Request request) {
        // 初始化
        MessageChain messageChain = EmptyMessageChain.INSTANCE;
        List<String> messageList = splitMessage(response);
        for (String message : messageList) {
            String tag = checkTag(message);
            if (tag == null) {
                messageChain = messageChain.plus(message);
            } else if ("at".equals(tag)) {
                long id = Long.parseLong(message.substring(4, message.length() - 5));
                messageChain = messageChain.plus(new At(request.getGroup().get(id)));
            } else if ("face".equals(tag)) {
                int id = Integer.parseInt(message.substring(6, message.length() - 7));
                messageChain = messageChain.plus(new Face(id));
            } else if ("image".equals(tag)) {
                String id = message.substring(7, message.length() - 8);
                messageChain = messageChain.plus(uploadImage(request, new File(CHAT_PIC_DIR + id)));
            }
        }
        return messageChain;
    }

    /**
     * 响应查询词库结果
     * @param keywordList 关键词列表
     * @param request 请求
     * @return 返回
     */
    private static MessageChain responseKeywords(List<Keyword> keywordList, Request request) {
        MessageChain messageChain = EmptyMessageChain.INSTANCE;
        for (Keyword keyword : keywordList) {
            messageChain = messageChain.plus("\n----------\n");
            messageChain = messageChain.plus("ID: " + keyword.getId() + "\n关键词：");
            messageChain = messageChain.plus(responsePattern(keyword.getKeyword(), request));
            messageChain = messageChain.plus("\n回复内容: ");
            messageChain = messageChain.plus(responsePattern(keyword.getResponse(), request));
            messageChain = messageChain.plus("\n添加人：" + keyword.getCreatorId());
        }
        return messageChain;
    }

    /**
     * 获取用户唯一标识
     * @param request 请求信息
     * @return 用户标识
     */
    private static String getUserId(Request request) {
        if (request.getMessageSource().equals(MessageSource.GROUP)) {
            return request.getGroup().getId() + ":" + request.getFrom();
        } else {
            return String.valueOf(request.getFrom());
        }
    }

    /**
     * 构建群关键词映射
     * @param groupId 群ID
     */
    private static HashMap<String, List<Keyword>> buildKeywordMap(long groupId) {
        List<Keyword> keywordList = Dao.findKeywords(groupId);
        HashMap<String, List<Keyword>> keywordMap = new HashMap<>();
        for (Keyword keyword : keywordList) {
            if (!keywordMap.containsKey(keyword.getKeyword())) {
                keywordMap.put(keyword.getKeyword(), new ArrayList<>());
            }
            keywordMap.get(keyword.getKeyword()).add(keyword);
        }
        return keywordMap;
    }

    /**
     * 将消息划分为tag段
     * @param content 消息体
     * @return tag段
     */
    public static List<String> splitMessage(String content) {
        // 初始化
        List<String> messageList = new ArrayList<>();
        messageList.add(content);

        // 遍历所有的消息，分割tag
        String[] validTags = new String[] {"at", "face", "image"};
        for (String tag : validTags) {
            List<String> newMessageList = new ArrayList<>();
            for (String message : messageList) {
                // 对于非划分好的消息，进行分割
                if (checkTag(message) == null) {
                    String[] messagePart1 = message.split("<" + tag + ">");
                    newMessageList.add(messagePart1[0]);
                    for (int i = 1; i < messagePart1.length; i ++) {
                        // 最多划分两份
                        String[] messagePart2 = messagePart1[i].split("</" + tag + ">", 2);
                        if (messagePart2.length > 1) {
                            newMessageList.add("<" + tag + ">" + messagePart2[0] + "</" + tag + ">");
                            if (messagePart2[1].length() > 0) {
                                newMessageList.add(messagePart2[1]);
                            }
                        } else {
                            newMessageList.add("<" + tag + ">" + messagePart1[i]);
                        }
                    }
                } else {
                    newMessageList.add(message);
                }
            }
            messageList = newMessageList;
        }
        return messageList;
    }

    /**
     * 检查一段文字是否被tag包围，如果是则返回tag
     * @param content 文本
     * @return tag，没有时为null
     */
    private static String checkTag(String content) {
        String[] validTags = new String[] {"at", "face", "image"};
        for (String tag : validTags) {
            if (content.startsWith(String.format("<%s>", tag)) && content.endsWith(String.format("</%s>", tag))) {
                return tag;
            }
        }
        return null;
    }

    /**
     * 保存图片
     * @param request 请求
     * @param query 请求内容
     * @return 是否保存成功
     */
    private static boolean saveImage(Request request, String query) {
        String[] valueParts = query.split("<image>");
        for (int i = 1; i < valueParts.length; i ++) {
            String imageId = valueParts[i].split("</image>")[0];
            File file = new File(CHAT_PIC_DIR + imageId);
            if (!file.exists()) {
                String url = request.getBot().queryImageUrl(request.getImageMap().get(imageId));
                file = FileHelper.download(url, file);
                if (file == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 上传图片
     * @param request 请求
     * @param image 上传后的图片文件
     * @return messageChain
     */
    private static MessageChain uploadImage(Request request, File image) {
        boolean accept;
        synchronized (DialogService.class) {
            if (! IMAGE_FLAG) {
                IMAGE_FLAG = true;
                accept = true;
            } else {
                accept = false;
            }
        }
        if (!accept) {
            return EmptyMessageChain.INSTANCE.plus("[图片]");
        }
        try {
            if (request.getMessageSource().equals(MessageSource.GROUP)) {
                // 备注：虽然不知道为什么这里要上传两遍，但是少任意一个都不能用……
                // UPDATE: 好像不加这句可以用了，诶嘿
                // request.getGroup().getBotAsMember().uploadImage(image);
                return EmptyMessageChain.INSTANCE.plus(request.getGroup().uploadImage(image));
            } else {
                return EmptyMessageChain.INSTANCE.plus(request.getBot().getSelfQQ().uploadImage(image));
            }
        } finally {
            synchronized (DialogService.class) {
                IMAGE_FLAG = false;
            }
        }

    }

    /**
     * 从query中提取成员标签
     * @param content 内容
     * @return 成员tag
     */
    private static UnitTag extractUnitName(String content) {
        if (content.contains("南小鸟")) {
            return new UnitTag(content.replaceFirst("南小鸟", ""), "南琴梨");
        } else if (content.contains("小鸟")) {
            return new UnitTag(content.replaceFirst("小鸟", ""), "南琴梨");
        } else if (content.contains("鸟")) {
            return new UnitTag(content.replaceFirst("鸟", ""), "南琴梨");
        } else if (content.contains("南琴梨")) {
            return new UnitTag(content.replaceFirst("南琴梨", ""), "南琴梨");
        } else if (content.contains("琴梨")) {
            return new UnitTag(content.replaceFirst("琴梨", ""), "南琴梨");
        } else if (content.contains("高坂穗乃果")) {
            return new UnitTag(content.replaceFirst("高坂穗乃果", ""), "高坂穗乃果");
        } else if (content.contains("穗乃果")) {
            return new UnitTag(content.replaceFirst("穗乃果", ""), "高坂穗乃果");
        } else if (content.contains("果")) {
            return new UnitTag(content.replaceFirst("果", ""), "高坂穗乃果");
        } else if (content.contains("园田海未")) {
            return new UnitTag(content.replaceFirst("园田海未", ""), "园田海未");
        } else if (content.contains("海未")) {
            return new UnitTag(content.replaceFirst("海未", ""), "园田海未");
        } else if (content.contains("海")) {
            return new UnitTag(content.replaceFirst("海", ""), "园田海未");
        } else if (content.contains("星空凛")) {
            return new UnitTag(content.replaceFirst("星空凛", ""), "星空凛");
        } else if (content.contains("凛")) {
            return new UnitTag(content.replaceFirst("凛", ""), "星空凛");
        } else if (content.contains("小泉花阳")) {
            return new UnitTag(content.replaceFirst("小泉花阳", ""), "小泉花阳");
        } else if (content.contains("花阳")) {
            return new UnitTag(content.replaceFirst("花阳", ""), "小泉花阳");
        } else if (content.contains("花")) {
            return new UnitTag(content.replaceFirst("花", ""), "小泉花阳");
        } else if (content.contains("西木野真姬")) {
            return new UnitTag(content.replaceFirst("西木野真姬", ""), "西木野真姬");
        } else if (content.contains("真姬")) {
            return new UnitTag(content.replaceFirst("真姬", ""), "西木野真姬");
        } else if (content.contains("姬")) {
            return new UnitTag(content.replaceFirst("姬", ""), "西木野真姬");
        } else if (content.contains("矢泽妮可")) {
            return new UnitTag(content.replaceFirst("矢泽妮可", ""), "矢泽日香");
        } else if (content.contains("妮可")) {
            return new UnitTag(content.replaceFirst("妮可", ""), "矢泽日香");
        } else if (content.contains("妮")) {
            return new UnitTag(content.replaceFirst("妮", ""), "矢泽日香");
        } else if (content.contains("矢泽日香")) {
            return new UnitTag(content.replaceFirst("矢泽日香", ""), "矢泽日香");
        } else if (content.contains("日香")) {
            return new UnitTag(content.replaceFirst("日香", ""), "矢泽日香");
        } else if (content.contains("东条希")) {
            return new UnitTag(content.replaceFirst("东条希", ""), "东条希");
        } else if (content.contains("希")) {
            return new UnitTag(content.replaceFirst("希", ""), "东条希");
        } else if (content.contains("绚濑绘里")) {
            return new UnitTag(content.replaceFirst("绚濑绘里", ""), "绚濑绘里");
        } else if (content.contains("绘里")) {
            return new UnitTag(content.replaceFirst("绘里", ""), "绚濑绘里");
        } else if (content.contains("绘")) {
            return new UnitTag(content.replaceFirst("绘", ""), "绚濑绘里");
        } else if (content.contains("高海千歌")) {
            return new UnitTag(content.replaceFirst("高海千歌", ""), "高海千歌");
        } else if (content.contains("千歌")) {
            return new UnitTag(content.replaceFirst("千歌", ""), "高海千歌");
        } else if (content.contains("千")) {
            return new UnitTag(content.replaceFirst("千", ""), "高海千歌");
        } else if (content.contains("樱内梨子")) {
            return new UnitTag(content.replaceFirst("樱内梨子", ""), "樱内梨子");
        } else if (content.contains("梨子")) {
            return new UnitTag(content.replaceFirst("梨子", ""), "樱内梨子");
        } else if (content.contains("梨")) {
            return new UnitTag(content.replaceFirst("梨", ""), "樱内梨子");
        } else if (content.contains("松浦果南")) {
            return new UnitTag(content.replaceFirst("松浦果南", ""), "松浦果南");
        } else if (content.contains("果南")) {
            return new UnitTag(content.replaceFirst("果南", ""), "松浦果南");
        } else if (content.contains("南")) {
            return new UnitTag(content.replaceFirst("南", ""), "松浦果南");
        } else if (content.contains("黑泽黛雅")) {
            return new UnitTag(content.replaceFirst("黑泽黛雅", ""), "黑泽黛雅");
        } else if (content.contains("黛雅")) {
            return new UnitTag(content.replaceFirst("黛雅", ""), "黑泽黛雅");
        } else if (content.contains("黛")) {
            return new UnitTag(content.replaceFirst("黛", ""), "黑泽黛雅");
        } else if (content.contains("渡边曜")) {
            return new UnitTag(content.replaceFirst("渡边曜", ""), "渡边曜");
        } else if (content.contains("曜")) {
            return new UnitTag(content.replaceFirst("曜", ""), "渡边曜");
        } else if (content.contains("津岛善子")) {
            return new UnitTag(content.replaceFirst("津岛善子", ""), "津岛善子");
        } else if (content.contains("善子")) {
            return new UnitTag(content.replaceFirst("善子", ""), "津岛善子");
        } else if (content.contains("善")) {
            return new UnitTag(content.replaceFirst("善", ""), "津岛善子");
        } else if (content.contains("国木田花丸")) {
            return new UnitTag(content.replaceFirst("国木田花丸", ""), "国木田花丸");
        } else if (content.contains("花丸")) {
            return new UnitTag(content.replaceFirst("花丸", ""), "国木田花丸");
        } else if (content.contains("丸")) {
            return new UnitTag(content.replaceFirst("丸", ""), "国木田花丸");
        } else if (content.contains("小原鞠莉")) {
            return new UnitTag(content.replaceFirst("小原鞠莉", ""), "小原鞠莉");
        } else if (content.contains("鞠莉")) {
            return new UnitTag(content.replaceFirst("鞠莉", ""), "小原鞠莉");
        } else if (content.contains("鞠")) {
            return new UnitTag(content.replaceFirst("鞠", ""), "小原鞠莉");
        } else if (content.contains("黑泽露比")) {
            return new UnitTag(content.replaceFirst("黑泽露比", ""), "黑泽露比");
        } else if (content.contains("露比")) {
            return new UnitTag(content.replaceFirst("露比", ""), "黑泽露比");
        } else if (content.contains("露")) {
            return new UnitTag(content.replaceFirst("露", ""), "黑泽露比");
        } else {
            return new UnitTag(content, null);
        }
    }

    /**
     * 生成命座/精炼信息
     * @param genshinUnit 角色信息
     * @return 命座/精炼信息
     */
    private static String generateLevelInfo(GenshinUnit genshinUnit) {
        UnitType unitType = UnitType.getById(genshinUnit.getUnitType());
        String levelInfo;
        if (WishHelper.isFull(genshinUnit)) {
            int overflowCount = genshinUnit.getLevel() - unitType.getFullSize();
            if (genshinUnit.getRarity() == 5) {
                levelInfo = String.format("(满%s，溢出%d)", unitType.getUnitName(), overflowCount);
            } else {
                levelInfo = String.format("(满%s)", unitType.getUnitName());
            }
        } else {
            int level = genshinUnit.getLevel() - unitType.getLevelBias();
            levelInfo = String.format("(%d%s)", level, unitType.getUnitName());
        }
        return levelInfo;
    }

    private static class ResponseFlag {
        private boolean needAt;
        ResponseFlag() {
            needAt = true;
        }
    }

    private static class UnitTag {
        private final String tag;
        private final String name;
        UnitTag(String tag, String name) {
            this.tag = tag;
            this.name = name;
        }
    }

}
