package processor.dialogue;

import com.alibaba.fastjson.JSON;
import dao.Dao;
import entity.service.DbSaint;
import entity.service.PrimoGems;
import entity.service.Property;
import entity.service.PropertyEnum;
import entity.service.Request;
import entity.service.Saint;
import entity.service.SaintScore;
import entity.service.SaintSuit;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import utils.SaintHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

/**
 * @author guohaohao
 */
@Slf4j
public class SaintDialogService {

    private static final String SAINT_SCORE_HELP_RESPONSE = "\n(请复制本消息并删除到【实际副词条】信息，发送回群中)\n"
            + "【圣遗物评分】\n"
            + "位置：花\n"
            + "等级：20\n"
            + "暴击率：0%\n"
            + "暴击伤害：0%\n"
            + "...(请补全全部词条)";
    private static final String SAINT_SCORE_RESULT = "双暴攻击：%.1f\n"
            + "百分制分数：%.2f\n"
            + "%s(+%d)分位：%s%%\n"
            + "圣遗物价值：%s体力\n"
            + "评级：%s%s";
    private static final String SAINT_SCORE_RESULT_RESPONSE = "\n【圣遗物详情】\n"
            + "%s\n"
            + "----------\n"
            + "【评分结果】\n"
            + "%s\n"
            + "----------\n"
            + "(注1：本评分系统仅适用于常规攻击+双爆输出模型，3、4、5号位要求主属性为：攻击|元素伤害|双暴)\n"
            + "(注2：双暴攻击公式：S = 暴击率*2 + 暴击伤害 + 大攻击 + 小攻击*0.15)";
    private static final String[] POS_DETAIL = new String[] {"生之花", "死之羽", "时之沙", "空之杯", "理之冠"};
    private static final double[][] MAX_SCORE = new double[][] {
            {50.0628125, 47.2128125, 44.2628125, 50.0628125, 42.2628125},
            {52.70025,   49.85025,   46.90025,   52.70025,   44.90025},
            {55.3376875, 52.4876875, 49.5376875, 55.3376875, 47.5376875},
            {57.975125,  55.125125,  52.175125,  57.975125,  50.175125},
            {60.6125625, 57.7625625, 54.8125625, 60.6125625, 52.8125625},
            {63.15,      60.3,       57.35,      63.15,      55.25}
    };
    private static final Random RANDOM = new Random();
    private static final String SAINT_PIC_DIR = "./pics/saint/";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH");

    /**
     * 进行圣遗物评分
     * @param request 请求体
     * @return 返回
     */
    public static MessageChain saintScore(Request request) {
        log.info("Saint score found");
        try {
            if (!request.getQuery().contains("【圣遗物评分】")) {
                return EmptyMessageChain.INSTANCE.plus(SAINT_SCORE_HELP_RESPONSE);
            }
            request.setQuery(request.getQuery().replaceAll(":", "："));
            String[] saintInfo = request.getQuery().split("【圣遗物评分】\\n", 2)[1].split("\\n", 7);
            if (!saintInfo[0].startsWith("位置：") || !saintInfo[1].startsWith("等级：")) {
                return EmptyMessageChain.INSTANCE.plus(SAINT_SCORE_HELP_RESPONSE);
            }
            int pos = SaintHelper.getPos(saintInfo[0].split("：", 2)[1]);
            int level = Integer.parseInt(saintInfo[1].split("：", 2)[1]);
            Saint saint = new Saint(pos, level);
            for (int i = 2; i < 6; i ++) {
                if (saintInfo.length <= i || !saintInfo[i].contains("：")) {
                    break;
                }
                String[] parts = saintInfo[i].split("：", 2);
                if (parts[0].contains("攻击")) {
                    if (parts[0].contains("比") || parts[0].startsWith("大") || parts[1].endsWith("%")) {
                        saint.getSubProperties().add(new Property(PropertyEnum.ATK_RATIO, Double.parseDouble(parts[1].replace("%", ""))));
                    } else {
                        saint.getSubProperties().add(new Property(PropertyEnum.ATK, Double.parseDouble(parts[1])));
                    }
                } else if (parts[0].contains("防御")) {
                    if (parts[0].contains("比") || parts[0].startsWith("大") || parts[1].endsWith("%")) {
                        saint.getSubProperties().add(new Property(PropertyEnum.DEF_RATIO, Double.parseDouble(parts[1].replace("%", ""))));
                    } else {
                        saint.getSubProperties().add(new Property(PropertyEnum.DEF, Double.parseDouble(parts[1])));
                    }
                } else if (parts[0].contains("生命")) {
                    if (parts[0].contains("比") || parts[0].startsWith("大") || parts[1].endsWith("%")) {
                        saint.getSubProperties().add(new Property(PropertyEnum.HP_RATIO, Double.parseDouble(parts[1].replace("%", ""))));
                    } else {
                        saint.getSubProperties().add(new Property(PropertyEnum.HP, Double.parseDouble(parts[1])));
                    }
                } else if (parts[0].contains("精通")) {
                    saint.getSubProperties().add(new Property(PropertyEnum.EM, Double.parseDouble(parts[1])));
                } else if (parts[0].contains("充能")) {
                    saint.getSubProperties().add(new Property(PropertyEnum.ENERGY, Double.parseDouble(parts[1].replace("%", ""))));
                } else if (parts[0].contains("伤")) {
                    saint.getSubProperties().add(new Property(PropertyEnum.CRITICAL_DMG, Double.parseDouble(parts[1].replace("%", ""))));
                } else if (parts[0].contains("暴")) {
                    saint.getSubProperties().add(new Property(PropertyEnum.CRITICAL_PROB, Double.parseDouble(parts[1].replace("%", ""))));
                }
            }
            SaintHelper.score(saint);
            String response = String.format(SAINT_SCORE_RESULT_RESPONSE, saint.toString(), getScoreString(saint));
            return EmptyMessageChain.INSTANCE.plus(response);
        } catch (Exception e) {
            log.error("Saint score error", e);
            return EmptyMessageChain.INSTANCE.plus(SAINT_SCORE_HELP_RESPONSE);
        }
    }

    /**
     * 获取圣遗物
     * @param request 请求
     * @param matcher matcher
     * @return 返回
     */
    public static MessageChain wish(Request request, Matcher matcher) {
        log.info("Saint wish found");

        // 预处理
        int enable = 0;
        String wishCostString = matcher.group(1);
        if ("抽".equals(wishCostString)) {
            enable = 1;
        }
        String suitName = matcher.group(2);
        if (suitName.contains("套")) {
            suitName = suitName.split("套")[0];
        } else {
            suitName = "";
        }
        int wishMode = 0;
        Integer dbWishMode = Dao.getWishMode(request.getFrom());
        if (dbWishMode != null) {
            wishMode = dbWishMode;
        }
        log.info("wishMode: {}, suitName: {}", wishMode, suitName);

        // 校验时间
        if (enable == 0) {
            int h = Integer.parseInt(SIMPLE_DATE_FORMAT.format(new Date()));
            if (h >= 8) {
                return EmptyMessageChain.INSTANCE.plus("\n为避免打扰正常聊天，白嫖圣遗物仅可以在0:00~8:00进行哦！（更加推荐私聊抹布）");
            }
        }

        // 校验树脂数量
        PrimoGems primoGemsInfo = Dao.getPrimogems(request.getFrom());
        int resin = 0;
        if (primoGemsInfo != null) {
            resin = primoGemsInfo.getResin();
        }
        if (resin == 0 && enable == 1) {
            return EmptyMessageChain.INSTANCE.plus("\n抽圣遗物失败，树脂不足！\n当前浓缩树脂数：" + resin + "个");
        }

        // 校验套装名称
        List<SaintSuit> saintSuit = Dao.getSaintSuit(suitName);
        if (saintSuit.isEmpty()) {
            return EmptyMessageChain.INSTANCE.plus("\n抽圣遗物失败，套装不存在！");
        }

        // 抽圣遗物与结果转义
        List<Saint> saintList = new ArrayList<>();
        List<DbSaint> dbSaintList = new ArrayList<>();
        List<String> saintName = new ArrayList<>();
        for (int i = 0; i < 2; i ++) {
            int count = RANDOM.nextDouble() < 0.065 ? 2 : 1;
            for (int j = 0; j < count; j ++) {
                Saint saint = Saint.generateSaint(saintSuit);
                SaintHelper.score(saint);
                log.info("Create saint: {}", JSON.toJSONString(saint));
                DbSaint dbSaint = new DbSaint(request.getFrom(), saint);
                dbSaint.setEnable(enable);
                Dao.addSaintWish(dbSaint);
                saintList.add(saint);
                dbSaintList.add(dbSaint);
                if (wishMode == 0) {
                    saintName.add(saint.getName());
                }
            }
        }
        log.info("Saint count = {}", saintList.size());
        Dao.addPrimogems(request.getFrom(), 0, 0, -enable);
        StringBuilder stringBuilder = new StringBuilder("\n抽圣遗物成功！花费浓缩树脂").append(enable).append("个，剩余浓缩树脂")
                .append(resin - enable)
                .append("个\n圣遗物结果：\n");

        for (int i = 0; i < saintList.size(); i ++) {
            Saint saint = saintList.get(i);
            stringBuilder.append("[").append(dbSaintList.get(i).getId()).append("]");
            stringBuilder.append(Saint.POSITION_NAME[saint.getPos()])
                    .append(" ")
                    .append(saint.getMainProperty().getProperty().getName())
                    .append("\n(得分：")
                    .append(String.format("%.1f", saint.getSaintScore().getScore()))
                    .append(")\n");
            for (int j = 0; j < 2; j ++) {
                Property subProperty = saint.getSubProperties().get(j);
                stringBuilder.append(subProperty.getProperty().getName()).append(" ");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append("\n");
            for (int j = 2; j < saint.getSubProperties().size(); j ++) {
                Property subProperty = saint.getSubProperties().get(j);
                stringBuilder.append(subProperty.getProperty().getName()).append(" ");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append("\n\n");

        }

        MessageChainBuilder messageChain = new MessageChainBuilder().append(stringBuilder);

        // 图片展示
        for (String name : saintName) {
            File file = new File(SAINT_PIC_DIR + name + ".png");
            if (file.exists()) {
                messageChain = messageChain.append(UtilDialogService.uploadImage(request, file)).append("\n");
            }
        }

        if (enable == 0) {
            messageChain = messageChain.append("当前为白嫖模式，圣遗物不计入统计");
        } else {
            messageChain = messageChain.append("使用“查看圣遗物{id}”查看圣遗物详情，使用“强化圣遗物{id}”可以进行强化\n(tips: “设置图片模式普通”将支持查看图片，但可能造成卡顿，请谨慎选择)");
        }
        return messageChain.build();
    }

    /**
     * 强化圣遗物
     * @param request 请求
     * @param matcher matcher
     * @return 返回
     */
    public static MessageChain strength(Request request, Matcher matcher) {
        log.info("Saint strength find");
        int saintId = Integer.parseInt(matcher.group(1));
        DbSaint dbSaint = Dao.getSaint(saintId);
        if (dbSaint == null) {
            return EmptyMessageChain.INSTANCE.plus("\n圣遗物ID不存在！");
        }
        if (dbSaint.getUserId() != request.getFrom()) {
            return EmptyMessageChain.INSTANCE.plus("\n仅支持强化自己的圣遗物！");
        }
        if (dbSaint.getLevel() == 20) {
            return EmptyMessageChain.INSTANCE.plus("\n圣遗物已强化！");
        }

        Saint saint = new Saint(dbSaint);
        StringBuilder strengthProcess = new StringBuilder();
        for (int i = 0; i < 5; i ++) {
            strengthProcess.append(saint.strengthen());
        }
        SaintHelper.score(saint);
        DbSaint newDbSaint = new DbSaint(request.getFrom(), saint);
        newDbSaint.setId(dbSaint.getId());
        newDbSaint.setWishTime(dbSaint.getWishTime());
        Dao.updateSaint(newDbSaint);
        StringBuilder stringBuilder = new StringBuilder("\n强化圣遗物成功\n圣遗物ID：")
                .append(saintId)
                .append("\n【强化过程】\n")
                .append(strengthProcess)
                .append("【强化结果】\n")
                .append(saint.toString())
                .append("\n----------\n")
                .append(getScoreString(saint));
        return EmptyMessageChain.INSTANCE.plus(stringBuilder);
    }

    /**
     * 查看圣遗物
     * @param request 请求
     * @param matcher matcher
     * @return 返回
     */
    public static MessageChain find(Request request, Matcher matcher) {
        log.info("Saint find find");

        // 预处理
        int wishMode = 0;
        Integer dbWishMode = Dao.getWishMode(request.getFrom());
        if (dbWishMode != null) {
            wishMode = dbWishMode;
        }

        int saintId = Integer.parseInt(matcher.group(1));
        DbSaint dbSaint = Dao.getSaint(saintId);
        if (dbSaint == null) {
            return EmptyMessageChain.INSTANCE.plus("\n圣遗物ID不存在！");
        }
        Saint saint = new Saint(dbSaint);
        SaintHelper.score(saint);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n圣遗物ID：")
                .append(saintId)
                .append("\n拥有者：")
                .append(dbSaint.getEnable() == 0 ? "无" : dbSaint.getUserId())
                .append("\n")
                .append(saint.toString())
                .append("\n----------\n")
                .append(getScoreString(saint));

        MessageChainBuilder messageChain = new MessageChainBuilder().append(stringBuilder);

        if (wishMode == 0) {
            File file = new File(SAINT_PIC_DIR + saint.getName() + ".png");
            if (file.exists()) {
                messageChain = messageChain.append("\n").append(UtilDialogService.uploadImage(request, file));
            }
        }

        if (saint.getLevel() < 20) {
            messageChain = messageChain.append("\n\n使用“强化圣遗物{id}”可以进行强化");
        }
        return messageChain.build();
    }

    /**
     * 圣遗物统计
     * @param request 请求
     * @return 返回
     */
    public static MessageChain mySaint(Request request) {
        log.info("My Saint find");
        int count = Dao.getSaintCount(request.getFrom());
        List<DbSaint> bestSaintList = Dao.getBestSaint(request.getFrom(), 10);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n圣遗物总数：").append(count).append("\n评级最高的10个圣遗物：\n");
        for (DbSaint dbSaint : bestSaintList) {
            stringBuilder.append(dbSaint.getId())
                    .append(". ")
                    .append(dbSaint.getSaintName())
                    .append(" [")
                    .append(POS_DETAIL[dbSaint.getPos()])
                    .append(" +")
                    .append(dbSaint.getLevel())
                    .append("]\n\t得分：")
                    .append(dbSaint.getScore(), 0, dbSaint.getScore().length() - 5)
                    .append("\t评级：")
                    .append(SaintHelper.getLevel(Double.parseDouble(dbSaint.getRatio())))
                    .append("\n");
        }
        stringBuilder.append("\n使用“查看圣遗物{id}”可以查看圣遗物详情");
        return EmptyMessageChain.INSTANCE.plus(stringBuilder);
    }

    /**
     * 圣遗物群排名
     * @param request 请求
     * @return 响应
     */
    public static MessageChain groupRank(Request request) {
        log.info("Group Saint find");
        List<DbSaint> bestSaintList = Dao.getBestSaint(0, 10);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n群内评级最高的10个圣遗物：\n");
        for (DbSaint dbSaint : bestSaintList) {
            stringBuilder.append(dbSaint.getId())
                    .append(". ")
                    .append(dbSaint.getSaintName())
                    .append(" [")
                    .append(POS_DETAIL[dbSaint.getPos()])
                    .append(" +")
                    .append(dbSaint.getLevel())
                    .append("]\n\t拥有者：")
                    .append(dbSaint.getUserId())
                    .append("\n\t得分：")
                    .append(dbSaint.getScore(), 0, dbSaint.getScore().length() - 5)
                    .append("\t评级：")
                    .append(SaintHelper.getLevel(Double.parseDouble(dbSaint.getRatio())))
                    .append("\n");
        }
        stringBuilder.append("\n使用“查看圣遗物{id}”可以查看圣遗物详情");
        return EmptyMessageChain.INSTANCE.plus(stringBuilder);
    }

    /**
     * 获取得分字符串
     * @param saint 圣遗物
     * @return 得分字符串
     */
    private static String getScoreString(Saint saint) {
        SaintScore score = saint.getSaintScore();
        String ratioString;
        if (score.getRatio() <= 90) {
            ratioString = String.format("%.0f", score.getRatio());
        } else {
            ratioString = String.format("%.5f", score.getRatio()).replaceAll("0{2,}", "");
            if (ratioString.endsWith(".")) {
                ratioString = ratioString.substring(0, ratioString.length() - 1);
            }
        }
        String valueString;
        if (score.getValue() > 1000000) {
            valueString = score.getValue() / 10000 + "万";
        } else {
            valueString = String.valueOf(score.getValue());
        }
        return String.format(SAINT_SCORE_RESULT,
                score.getScore(),
                100 * (score.getScore() + score.getLevelScore()) / MAX_SCORE[saint.getLevel() / 4][saint.getPos()],
                POS_DETAIL[saint.getPos()],
                saint.getLevel(),
                ratioString,
                valueString,
                score.getLevel(),
                saint.getName() == null && saint.getLevel() == 20 ? ("\n评价：" + score.getLevelComment()) : ""
        );
    }

}
