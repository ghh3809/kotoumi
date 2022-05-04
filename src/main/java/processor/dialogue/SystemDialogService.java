package processor.dialogue;

import constant.MessageSource;
import dao.Dao;
import entity.service.Daily;
import entity.service.Keyword;
import entity.service.PrimoGems;
import entity.service.Request;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import processor.DialogService;
import utils.FileHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统对话处理
 * @author guohaohao
 */
@Slf4j
public class SystemDialogService {

    /**
     * 回复模板
     */
    private static final String HELP_RESPONSE = "\n你好，这里是你的专属小可爱：可莉！快来找可莉一起玩吧！\n目前支持以下模板：\n"
            + "【原神相关】\n"
            + "查看原神关键词：原神\n"
            + "【群聊娱乐】\n"
            + "每日功能：签到/抽签/占卜\n"
            + "【SIF卡面查询】\n"
            + "查卡：查卡{编号/卡名称}[觉醒][无框]\n"
            + "【词库管理】\n"
            + "新增词库：问_{问题}_答_{答案}\n"
            + "查询词库：查询词库[{词库ID/关键词}][_起始_{起始编号}]\n"
            + "模糊查询词库：模糊查询词库{关键词}[_起始_{起始编号}]\n"
            + "模糊查询答案：模糊查询答案{关键词}[_起始_{起始编号}]\n"
            + "删除词库：删除词库{词库ID/关键词}\n"
            + "【抽卡】\n"
            + "抽卡：[普通/快速/无图/]抽卡/10连[标准池/角色池/武器池]\n"
            + "查看拥有角色：我的角色\n"
            + "查看拥有武器：我的武器\n"
            + "查看祈愿统计：我的统计\n"
            + "星辉换原石：星辉全部换原石\n"
            + "查看当前开放卡池：当前卡池\n"
            + "获取详细概率说明：概率说明\n"
            + "设置抽卡图片模式：设置图片模式[普通/快速/无图]\n"
            + "【圣遗物】\n"
            + "圣遗物评分：圣遗物评分\n"
            + "抽圣遗物：抽/白嫖圣遗物[{套装名称}套]\n"
            + "查看圣遗物：查看圣遗物{ID}\n"
            + "强化圣遗物：强化圣遗物{ID}\n"
            + "圣遗物统计：我的圣遗物\n"
            + "群圣遗物排名：圣遗物排名\n"
            + "【关于】\n"
            + "海鸟阁小机器人，请各位善待，如有需求或问题，欢迎随时反馈管理组！";
    /**
     * 原神菜单
     */
    private static final String GENSHIN_RESPONSE = "\n" +
            "【周常副本掉落】\n" +
            "天赋材料\n" +
            "武器材料\n" +
            "周常材料\n" +
            "----------\n" +
            "【原神机制数据】\n" +
            "圣遗物属性\n" +
            "养成计算\n" +
            "抗性系数\n" +
            "怪物抗性表\n" +
            "怪物血量表\n" +
            "元素附着时间\n" +
            "元素反应残留\n" +
            "----------\n" +
            "【角色与武器培养】\n" +
            "角色攻略\n" +
            "角色配装\n" +
            "武器攻略\n" +
            "----------\n" +
            "【概率查询】\n" +
            "抽卡概率\n" +
            "普池概率\n" +
            "角色概率\n" +
            "武器概率\n" +
            "抽卡机制\n" +
            "----------\n" +
            "【世界材料刷新点】\n" +
            "请用模糊查询功能，例如“模糊查询词库刀镡”";
    /**
     * 抽签占卜结果列表
     */
    private static final String[] DRAW_TEXT = new String[] {"大凶", "凶", "末吉", "小吉", "吉", "中吉", "中吉", "大吉", "大吉"};
    private static final String[] DIVINE_TEXT = new String[] {
            "单抽出货？不存在的。老老实实肝活动吧",
            "嗯！运气一般呢",
            "你今天运气不错呢",
            "哇！今天欧死了,会有UR吗？",
            "听说你想要的角色限定池快要到了？攒心抽说不定能出货呢！"
    };

    private static final String KEYWORD_QUERY_KEYWORD = "查询词库";
    private static final String KEYWORD_FUZZY_QUERY_KEYWORD = "模糊查询词库";
    private static final String KEYWORD_FUZZY_QUERY_ANSWER = "模糊查询答案";
    private static final String KEYWORD_DELETE_KEYWORD = "删除词库";
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{1,4}$");

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Random RANDOM = new Random();
    private static final Pattern KEYWORD_QUERY_PATTERN = Pattern.compile("^(.*?)(_起始_\\d+)?$");
    private static final String DRAW_PIC_DIR = "./pics/draw/";
    private static final String CHAT_PIC_DIR = "./pics/chat/";

    /**
     * 帮助菜单
     * @return 返回
     */
    public static MessageChain help() {
        log.info("Help found");
        return EmptyMessageChain.INSTANCE.plus(HELP_RESPONSE);
    }

    /**
     * 原神菜单
     * @return 返回
     */
    public static MessageChain genshin() {
        log.info("Genshin found");
        return EmptyMessageChain.INSTANCE.plus(GENSHIN_RESPONSE);
    }

    /**
     * 签到
     * @param request 请求
     * @return 返回
     */
    public static MessageChain signIn(Request request) {

        log.info("Sign in found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        // 获取当前daily
        String day = SIMPLE_DATE_FORMAT.format(new Date());
        Daily daily = Dao.findDailyById(request.getFrom(), day);
        if (daily == null) {
            Dao.addDaily(request.getGroup().getId(), request.getFrom());
            daily = Dao.findDailyById(request.getFrom(), day);
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
            int addResin = 5;

            // 签到前缀
            if (signInDays == 1) {
                addPrimogems = 20000;
                addResin = 100;
                Dao.createPrimogems(request.getFrom(), addPrimogems, addResin);
            } else {
                Dao.addPrimogems(request.getFrom(), addPrimogems, 0, addResin);
            }
            int primogems = 0;
            int resin = 0;
            PrimoGems primoGemsInfo = Dao.getPrimogems(request.getFrom());
            if (primoGemsInfo != null) {
                primogems = primoGemsInfo.getPrimogems();
                resin = primoGemsInfo.getResin();
            }

            return EmptyMessageChain.INSTANCE.plus("\n签到成功！\n累计签到" + signInDays + "天\n获得原石"
                    + addPrimogems + "个，剩余原石" + primogems + "个\n获得浓缩树脂" + addResin + "个，剩余浓缩树脂" + resin + "个");
        } else {
            return EmptyMessageChain.INSTANCE.plus("你今天已经签到过了，请明天再来~~");
        }
    }

    /**
     * 补签
     * @param request 请求
     * @return 返回
     */
    public static MessageChain addSignIn(Request request) {
        log.info("Add sign in found");
        return EmptyMessageChain.INSTANCE.plus("mabo暂时还不支持补签哦~");
    }

    /**
     * 抽签
     * @param request 请求
     * @return 返回
     */
    public static MessageChain draw(Request request) {

        log.info("Draw found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        // 获取当前daily
        String day = SIMPLE_DATE_FORMAT.format(new Date());
        Daily daily = Dao.findDailyById(request.getFrom(), day);
        if (daily == null) {
            Dao.addDaily(request.getGroup().getId(), request.getFrom());
            daily = Dao.findDailyById(request.getFrom(), day);
        }

        // 抽签
        if (daily.getDrawResult() == 0) {
            int drawResult = RANDOM.nextInt(DRAW_TEXT.length) + 1;
            Dao.updateDailyDraw(request.getGroup().getId(), request.getFrom(), day, drawResult);
            return new MessageChainBuilder().append("抽签成功！\n你今天的运势为：\n")
                    .append(UtilDialogService.uploadImage(request, new File(DRAW_PIC_DIR + drawResult + ".jpg")))
                    .build();
        } else {
            return EmptyMessageChain.INSTANCE.plus("你今天已经抽签过了，请明天再来~~\n你今天的运势为：" + DRAW_TEXT[daily.getDrawResult() - 1]);
        }
    }

    /**
     * 占卜
     * @param request 请求
     * @return 返回
     */
    public static MessageChain divine(Request request) {

        log.info("Divine found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        // 获取当前daily
        String day = SIMPLE_DATE_FORMAT.format(new Date());
        Daily daily = Dao.findDailyById(request.getFrom(), day);
        if (daily == null) {
            Dao.addDaily(request.getGroup().getId(), request.getFrom());
            daily = Dao.findDailyById(request.getFrom(), day);
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
    public static MessageChain substitute(Request request) {
        log.info("Substitute found");
        return EmptyMessageChain.INSTANCE.plus("mabo暂时还不支持关键词替换哦~");
    }

    /**
     * 用户添加词库
     * @param request 用户请求
     * @return 返回
     */
    public static MessageChain addKeyword(Request request, Matcher matcher) {

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
        DialogService.KEYWORD_MAP.put(request.getGroup().getId(), buildKeywordMap(request.getGroup().getId()));
        return new MessageChainBuilder().append("学到新知识了呢(ID: ")
                .append(String.valueOf(id))
                .append(")~不信你回复“")
                .append(UtilDialogService.responsePattern(key, request))
                .append("”试试~").build();
    }

    /**
     * 查询词库
     * @param request 请求
     * @return 返回
     */
    public static MessageChain queryKeyword(Request request, int fuzzyMode) {

        log.info("Query keyword found");

        if (!request.getMessageSource().equals(MessageSource.GROUP)) {
            return EmptyMessageChain.INSTANCE.plus("请在群中进行操作");
        }

        if (fuzzyMode == 0) {
            request.setQuery(request.getQuery().replaceFirst(KEYWORD_QUERY_KEYWORD, ""));
        } else if (fuzzyMode == 1) {
            request.setQuery(request.getQuery().replaceFirst(KEYWORD_FUZZY_QUERY_KEYWORD, ""));
        } else if (fuzzyMode == 2) {
            request.setQuery(request.getQuery().replaceFirst(KEYWORD_FUZZY_QUERY_ANSWER, ""));
        }

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
            if (CARD_NUMBER_PATTERN.matcher(query).find() && fuzzyMode == 0) {
                // 说明是按序号进行查看
                int keywordId = Integer.parseInt(query);
                Keyword keyword = Dao.findKeywordById(request.getGroup().getId(), keywordId);
                if (keyword == null) {
                    return EmptyMessageChain.INSTANCE.plus("词库不存在，请使用指令：“查询词库[{词库ID/关键词}][_起始_{起始编号}]”");
                } else {
                    return new MessageChainBuilder().append("查询到以下结果：")
                            .append(responseKeywords(Collections.singletonList(keyword), request)).build();
                }
            } else {
                // 说明是按关键词进行查看
                List<Keyword> keywordList = Dao.findKeywordByKey(request.getGroup().getId(), query, fuzzyMode, startIndex - 1);
                if (keywordList.isEmpty()) {
                    return EmptyMessageChain.INSTANCE.plus("词库不存在，请使用指令：“查询词库[{词库ID/关键词}][_起始_{起始编号}]”");
                } else {
                    int totalCount = Dao.countKeywordByKey(request.getGroup().getId(), query, fuzzyMode);
                    return new MessageChainBuilder().append("查询到以下结果：")
                            .append(responseKeywords(keywordList, request)).append("\n----------\n当前显示第")
                            .append(String.valueOf(startIndex)).append("-")
                            .append(String.valueOf(startIndex + keywordList.size() - 1))
                            .append("条，共").append(String.valueOf(totalCount)).append("条")
                            .build();
                }
            }
        } else {
            return EmptyMessageChain.INSTANCE.plus("请使用指令：“查询词库[{词库ID/关键词}][_起始_{起始编号}]”");
        }

    }

    /**
     * 删除群词库
     * @param request 请求
     * @return 返回
     */
    public static MessageChain deleteKeyword(Request request) {

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
                        Objects.requireNonNull(request.getGroup().get(request.getFrom())).getPermission()
                                .compareTo(MemberPermission.ADMINISTRATOR) >= 0) {
                    Dao.deleteKeyword(request.getGroup().getId(), keywordId);
                    DialogService.KEYWORD_MAP.put(request.getGroup().getId(), buildKeywordMap(request.getGroup().getId()));
                    return new MessageChainBuilder().append("已删除：")
                            .append(responseKeywords(Collections.singletonList(keyword), request))
                            .build();
                } else {
                    return EmptyMessageChain.INSTANCE.plus("无权删除，请联系词库创建者(" + keyword.getCreatorId() + ")或管理员删除");
                }
            }
        } else {
            // 说明是按关键词进行查看
            List<Keyword> keywordList = Dao.findKeywordByKey(request.getGroup().getId(), query, 0, 0);
            if (keywordList.isEmpty()) {
                return EmptyMessageChain.INSTANCE.plus("词库不存在，请使用指令：“删除词库{词库ID/关键词}”");
            } else {
                if (keywordList.size() == 1) {
                    Keyword keyword = keywordList.get(0);
                    if (request.getFrom() == keyword.getCreatorId() ||
                            Objects.requireNonNull(request.getGroup().get(request.getFrom())).getPermission()
                                    .compareTo(MemberPermission.ADMINISTRATOR) >= 0) {
                        Dao.deleteKeyword(request.getGroup().getId(), keyword.getId());
                        DialogService.KEYWORD_MAP.put(request.getGroup().getId(), buildKeywordMap(request.getGroup().getId()));
                        return new MessageChainBuilder().append("已删除：")
                                .append(responseKeywords(Collections.singletonList(keyword), request))
                                .build();
                    } else {
                        return EmptyMessageChain.INSTANCE.plus("无权删除，请联系词库创建者(" + keyword.getCreatorId() + ")或管理员删除");
                    }
                } else {
                    int totalCount = Dao.countKeywordByKey(request.getGroup().getId(), query, 0);
                    return new MessageChainBuilder().append("查询到以下结果：")
                            .append(responseKeywords(keywordList, request)).append("\n----------\n当前显示第1-")
                            .append(String.valueOf(keywordList.size())).append("条，共")
                            .append(String.valueOf(totalCount)).append("条。请直接回复“删除词库{词库ID}”进行删除")
                            .build();
                }
            }
        }
    }

    /**
     * 卡组pk
     * @param request 请求
     * @return 返回
     */
    public static MessageChain cardPk(Request request) {
        log.info("Card pk found");
        return EmptyMessageChain.INSTANCE.plus("mabo暂时还不支持卡组pk哦~");
    }

    /**
     * 保存图片
     * @param request 请求
     * @param query 请求内容
     * @return 是否保存成功
     */
    public static boolean saveImage(Request request, String query) {
        String[] valueParts = query.split("<image>");
        for (int i = 1; i < valueParts.length; i ++) {
            String imageId = valueParts[i].split("</image>")[0];
            File file = new File(CHAT_PIC_DIR + imageId);
            if (!file.exists()) {
                String url = Image.queryUrl(request.getImageMap().get(imageId));
                file = FileHelper.download(url, file);
                if (file == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 响应查询词库结果
     * @param keywordList 关键词列表
     * @param request 请求
     * @return 返回
     */
    public static MessageChain responseKeywords(List<Keyword> keywordList, Request request) {
        MessageChainBuilder messageChain = new MessageChainBuilder();
        for (Keyword keyword : keywordList) {
            messageChain = messageChain.append("\n----------\n");
            messageChain = messageChain.append("ID: ").append(String.valueOf(keyword.getId())).append("\n关键词：");
            messageChain = messageChain.append(UtilDialogService.responsePattern(keyword.getKeyword(), request));
            messageChain = messageChain.append("\n回复内容: ");
            messageChain = messageChain.append(UtilDialogService.responsePattern(keyword.getResponse(), request));
            messageChain = messageChain.append("\n添加人：").append(String.valueOf(keyword.getCreatorId()));
        }
        return messageChain.build();
    }

    /**
     * 构建群关键词映射
     * @param groupId 群ID
     */
    public static HashMap<String, List<Keyword>> buildKeywordMap(long groupId) {
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

}
