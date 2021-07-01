package processor.dialogue;

import com.alibaba.fastjson.JSON;
import constant.MultiTurnTask;
import dao.Dao;
import entity.service.EventRank;
import entity.service.MultiTurnStatus;
import entity.service.Request;
import entity.service.SifEvent;
import entity.service.Unit;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;
import processor.DialogService;
import utils.FileHelper;
import utils.RequestHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guohaohao
 */
@Slf4j
public class SifDialogService {

    private static final String QUERY_CARD_KEYWORD = "查卡";
    private static final String NO_BOX_KEYWORD = "无框";
    private static final String RANK_MAX_KEYWORD = "觉醒";
    private static final Pattern SKILL_LEVEL_PATTERN = Pattern.compile("(\\d{1,2})级");
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{1,4}$");
    private static final String CARD_PIC_DIR = "./pics/card/";
    private static final int MAX_CHOICE_NUMBER = 5;

    /**
     * 查看sif国服档线
     * @param request 请求
     * @return 返回
     */
    public static MessageChain sifRank(Request request) {
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
    public static MessageChain queryCard(Request request) {

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
                    DialogService.MULTI_TURN_STATUS_MAP.put(DialogService.getUserId(request), multiTurnStatus);
                    return EmptyMessageChain.INSTANCE.plus(stringBuilder.toString());
                }
            } else {
                return EmptyMessageChain.INSTANCE.plus("无查询结果，请使用指令：“查卡{编号/卡名称}[觉醒][无框]”");
            }
        }
    }

    /**
     * 根据卡的相册ID，获取卡面
     * @param cardNumber 卡相册ID
     * @param noBox 是否无框
     * @param rankMax 是否觉醒
     * @param request 请求
     * @return 响应
     */
    public static MessageChain responseCard(int cardNumber, boolean noBox, boolean rankMax, int skillLevel, Request request) {

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

        MessageChain messageChain = EmptyMessageChain.INSTANCE.plus(UtilDialogService.uploadImage(request, file));
        messageChain = messageChain.plus("\nID：" + cardNumber);
        if (skillDescription != null) {
            messageChain = messageChain.plus("\n技能(" + skillLevel + "级)：" + skillDescription);
        }
        return messageChain;
    }

    /**
     * 从query中提取成员标签
     * @param content 内容
     * @return 成员tag
     */
    public static UnitTag extractUnitName(String content) {
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

    private static class UnitTag {
        private final String tag;
        private final String name;
        UnitTag(String tag, String name) {
            this.tag = tag;
            this.name = name;
        }
    }

}
