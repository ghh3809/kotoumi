package processor;

import constant.MessageSource;
import constant.MultiTurnTask;
import dao.Dao;
import entity.service.Keyword;
import entity.service.MultiTurnStatus;
import entity.service.Request;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import processor.dialogue.SaintDialogService;
import processor.dialogue.SifDialogService;
import processor.dialogue.SystemDialogService;
import processor.dialogue.UtilDialogService;
import processor.dialogue.WishDialogService;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guohaohao
 */
@Slf4j
public class DialogService {

    public static final ConcurrentHashMap<String, MultiTurnStatus> MULTI_TURN_STATUS_MAP = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Long, HashMap<String, List<Keyword>>> KEYWORD_MAP = new ConcurrentHashMap<>();

    /**
     * query正则表达式
     */
    private static final Pattern CHOICE_PATTERN = Pattern.compile("^\\d$");
    private static final Pattern KEYWORD_ADD_PATTERN = Pattern.compile("^问[ _](.+?)[ _]答[ _](.+)$", Pattern.DOTALL);
    private static final Pattern WISH_PATTERN = Pattern.compile("^(普通|快速|无图|)(抽卡|单抽|10连|十连)(.+?)池.*$");
    private static final Pattern WISH_SAINT_PATTERN = Pattern.compile("^(抽|白嫖)圣遗物(.*)$");
    private static final Pattern STRENGTH_SAINT_PATTERN = Pattern.compile("^强化圣遗物([0-9]+).*$");
    private static final Pattern FIND_SAINT_PATTERN = Pattern.compile("^查看圣遗物([0-9]+).*$");
    private static final Pattern ADD_PRIMOGEMS_PATTERN = Pattern.compile("^氪金_(.+?)_(.+)$");
    private static final Pattern SIF_RANK_PATTERN = Pattern.compile("^(国服|当前|实时)?档线$");
    private static final Pattern WISH_RESULT_PATTERN = Pattern.compile("^我的(.+)$");
    private static final Pattern MODE_SET_PATTERN = Pattern.compile("^设置图片模式(普通|快速|无图)$");
    /**
     * query关键字
     */
    private static final String KLEE_KEYWORD = "可莉";
    private static final String HELP_KEYWORD = "帮助";
    private static final String GENSHIN_KEYWORD = "原神";
    private static final String QUERY_CARD_KEYWORD = "查卡";
    private static final String KEYWORD_QUERY_KEYWORD = "查询词库";
    private static final String KEYWORD_FUZZY_QUERY_KEYWORD = "模糊查询词库";
    private static final String KEYWORD_FUZZY_QUERY_ANSWER = "模糊查询答案";
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
    private static final String SAINT_SCORE_KEYWORD1 = "圣遗物评分";
    private static final String SAINT_SCORE_KEYWORD2 = "圣遗物分数";
    private static final String SAINT_RANK_KEYWORD = "圣遗物排名";

    private static final Random RANDOM = new Random();

    static {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> Dao.getSaint(1), 1, 1, TimeUnit.MINUTES);
    }

    public static MessageChain response(MessageChainBuilder at, Request request) {
        ResponseFlag responseFlag = new ResponseFlag();
        MessageChain messageChain = response(request, responseFlag);
        if (messageChain != null) {
            if (at != null && responseFlag.needAt) {
                return at.append(messageChain).build();
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
                        return SifDialogService.responseCard(multiTurnStatus.getCardNumbers().get(choice - 1),
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

        // 搜索内置关键字
        Matcher rankMatcher = SIF_RANK_PATTERN.matcher(request.getQuery());
        Matcher addKeywordMatcher = KEYWORD_ADD_PATTERN.matcher(request.getQuery());
        Matcher wishMatcher = WISH_PATTERN.matcher(request.getQuery());
        Matcher addPrimogemsMatcher = ADD_PRIMOGEMS_PATTERN.matcher(request.getQuery());
        Matcher wishResultMatcher = WISH_RESULT_PATTERN.matcher(request.getQuery());
        Matcher modeSetMatcher = MODE_SET_PATTERN.matcher(request.getQuery());
        Matcher wishSaintMatcher = WISH_SAINT_PATTERN.matcher(request.getQuery());
        Matcher strengthSaintMatcher = STRENGTH_SAINT_PATTERN.matcher(request.getQuery());
        Matcher findSaintMatcher = FIND_SAINT_PATTERN.matcher(request.getQuery());
        if (request.getQuery().equals(HELP_KEYWORD) || request.getQuery().equals(KLEE_KEYWORD)) {
            // 帮助
            return SystemDialogService.help();
        } else if (request.getQuery().equals(GENSHIN_KEYWORD)) {
            // 签到
            return SystemDialogService.genshin();
        } else if (request.getQuery().equals(SIGN_IN_KEYWORD)) {
            // 签到
            return SystemDialogService.signIn(request);
        } else if (request.getQuery().equals(DRAW_KEYWORD)) {
            // 抽签
            return SystemDialogService.draw(request);
        } else if (request.getQuery().equals(DIVINE_KEYWORD)) {
            // 占卜
            return SystemDialogService.divine(request);
        } else if (request.getQuery().startsWith(QUERY_CARD_KEYWORD)) {
            // 查卡
            return SifDialogService.queryCard(request);
        } else if (request.getQuery().startsWith(KEYWORD_QUERY_KEYWORD)) {
            // 查询词库
            return SystemDialogService.queryKeyword(request, 0);
        } else if (request.getQuery().startsWith(KEYWORD_FUZZY_QUERY_KEYWORD)) {
            // 查询词库
            return SystemDialogService.queryKeyword(request, 1);
        } else if (request.getQuery().startsWith(KEYWORD_FUZZY_QUERY_ANSWER)) {
            // 查询词库
            return SystemDialogService.queryKeyword(request, 2);
        } else if (request.getQuery().startsWith(KEYWORD_DELETE_KEYWORD)) {
            // 删除词库
            return SystemDialogService.deleteKeyword(request);
        } else if (request.getQuery().startsWith(SUBSTITUTE_KEYWORD)) {
            // 设置替换关键词
            return SystemDialogService.substitute(request);
        } else if (wishMatcher.find()) {
            // 抽卡
            return WishDialogService.wish(request, wishMatcher);
        } else if (request.getQuery().startsWith(CARD_PK_KEYWORD)) {
            // 卡组pk
            return SystemDialogService.cardPk(request);
        } else if (request.getQuery().startsWith(ADD_SIGN_IN_KEYWORD)) {
            // 补签
            return SystemDialogService.addSignIn(request);
        } else if (addKeywordMatcher.find()) {
            // 添加词库
            return SystemDialogService.addKeyword(request, addKeywordMatcher);
        } else if (wishResultMatcher.find()) {
            // 我的祈愿信息
            return WishDialogService.myWish(request, wishResultMatcher);
        } else if (request.getQuery().equals(TRANSFORM_KEYWORD)) {
            // 星辉全部换原石
            return WishDialogService.transform(request);
        } else if (request.getQuery().equals(PROB_KEYWORD)) {
            // 概率说明
            return WishDialogService.prob(request);
        } else if (addPrimogemsMatcher.find()) {
            // 增加原石
            return WishDialogService.addPrimogems(request, addPrimogemsMatcher);
        } else if (request.getQuery().equals(CURRENT_WISH_KEYWORD)) {
            // 当前卡池
            return WishDialogService.currentWish(request);
        } else if (rankMatcher.find()) {
            // 国服档线
            return SifDialogService.sifRank(request);
        } else if (modeSetMatcher.find()) {
            // 设置招募模式
            return WishDialogService.setMode(request, modeSetMatcher);
        } else if (request.getQuery().contains(SAINT_SCORE_KEYWORD1) || request.getQuery().contains(SAINT_SCORE_KEYWORD2)) {
            // 圣遗物评分
            return SaintDialogService.saintScore(request);
        } else if (wishSaintMatcher.find()) {
            // 抽圣遗物
            return SaintDialogService.wish(request, wishSaintMatcher);
        } else if (strengthSaintMatcher.find()) {
            // 强化圣遗物
            return SaintDialogService.strength(request, strengthSaintMatcher);
        } else if (findSaintMatcher.find()) {
            // 查看圣遗物
            return SaintDialogService.find(request, findSaintMatcher);
        } else if (request.getQuery().equals(SAINT_RANK_KEYWORD)) {
            // 群圣遗物排名
            return SaintDialogService.groupRank(request);
        }

        // 群自定义词库
        if (request.getMessageSource().equals(MessageSource.GROUP)) {
            if (!KEYWORD_MAP.containsKey(request.getGroup().getId())) {
                KEYWORD_MAP.put(request.getGroup().getId(), SystemDialogService.buildKeywordMap(request.getGroup().getId()));
            }
            List<Keyword> keywordList = KEYWORD_MAP.get(request.getGroup().getId()).get(request.getQuery());
            if (keywordList != null && !keywordList.isEmpty()) {
                Keyword keyword = keywordList.get(RANDOM.nextInt(keywordList.size()));
                log.info("Keyword query found: {}", keyword.getId());
                responseFlag.needAt = false;
                return UtilDialogService.responsePattern(keyword.getResponse(), request);
            }
        }

        // 均不满足时的回复
        return defaultResponse(request);
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
     * 获取用户唯一标识
     * @param request 请求信息
     * @return 用户标识
     */
    public static String getUserId(Request request) {
        if (request.getMessageSource().equals(MessageSource.GROUP)) {
            return request.getGroup().getId() + ":" + request.getFrom();
        } else {
            return String.valueOf(request.getFrom());
        }
    }

    private static class ResponseFlag {
        private boolean needAt;
        ResponseFlag() {
            needAt = true;
        }
    }

}
