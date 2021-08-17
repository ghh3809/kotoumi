package processor.dialogue;

import com.alibaba.fastjson.JSON;
import dao.Dao;
import entity.service.DbSaint;
import entity.service.PrimoGems;
import entity.service.Property;
import entity.service.Request;
import entity.service.SaintSuit;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.MessageChain;
import entity.service.Saint;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import utils.SaintHelper;
import entity.service.SaintScore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guohaohao
 */
@Slf4j
public class SaintDialogService {

    private static final String SAINT_SCORE_HELP_RESPONSE = "\n(请复制本消息并填写【副词条】信息，发送回群中)\n"
            + "【圣遗物评分】\n"
            + "位置：花\n"
            + "暴击率：0%\n"
            + "暴击伤害：0%\n"
            + "攻击力百分比：0%\n"
            + "攻击力：0";
    private static final String SAINT_SCORE_RESULT = "得分：%.1f\n"
            + "%s分位：%s%%\n"
            + "圣遗物价值：%s体力%s\n"
            + "评级：%s";
    private static final String SAINT_SCORE_RESULT_RESPONSE = "\n【圣遗物详情】\n"
            + "位置：%s\n"
            + "暴击率：%.1f%%\n"
            + "暴击伤害：%.1f%%\n"
            + "攻击力百分比：%.1f%%\n"
            + "攻击力：%d\n"
            + "----------\n"
            + "【评分结果(仅供参考)】\n"
            + "%s\n"
            + "----------\n"
            + "(注1：本评分系统使用公式：S = 暴击率*2 + 暴击伤害 + 大攻击 + 小攻击*0.15，元素精通和充能效率可视情况计入)\n"
            + "(注2：圣遗物价值在指定套装时，价值为2倍)";
    private static final String[] POS_DETAIL = new String[] {"生之花", "死之羽", "时之沙", "空之杯", "理之冠"};
    private static final Pattern SAINT_SCORE_PATTERN = Pattern.compile("^.*?位置：(.*?)暴击率：([0-9.]+)%?.*?暴击伤害：([0-9.]+)%?.*?攻击力百分比：([0-9.]+)%?.*?攻击力：([0-9.]+).*$", Pattern.DOTALL);
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
        Matcher matcher = SAINT_SCORE_PATTERN.matcher(request.getQuery());
        if (matcher.find()) {
            try {
                String posStr = matcher.group(1);
                double criticalProb = Double.parseDouble(matcher.group(2));
                double criticalDmg = Double.parseDouble(matcher.group(3));
                double atkRatio = Double.parseDouble(matcher.group(4));
                int atk = Integer.parseInt(matcher.group(5));
                int pos = SaintHelper.getPos(posStr);
                Saint saint = new Saint(pos, criticalProb, criticalDmg, atkRatio, atk);
                SaintHelper.score(saint);
                String response = String.format(SAINT_SCORE_RESULT_RESPONSE,
                        POS_DETAIL[pos],
                        criticalProb,
                        criticalDmg,
                        atkRatio,
                        atk,
                        getScoreString(saint)
                );
                return EmptyMessageChain.INSTANCE.plus(response);
            } catch (Exception e) {
                log.error("Saint score error", e);
                return EmptyMessageChain.INSTANCE.plus(SAINT_SCORE_HELP_RESPONSE);
            }
        } else {
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
        if (saint.getLevel() == 20) {
            SaintScore score = saint.getSaintScore();
            String ratioString;
            if (score.getRatio() <= 90) {
                ratioString = String.format("%.0f", score.getRatio());
            } else {
                ratioString = String.format("%.5f", score.getRatio()).replaceAll("0{2,}", "");
            }
            String valueString, valueString2;
            if (score.getValue() > 1000000) {
                valueString = score.getValue() / 10000 + "万";
                valueString2 = score.getValue() * 7 / 10000 + "万";
            } else {
                valueString = String.valueOf(score.getValue());
                valueString2 = String.valueOf(score.getValue() * 7);
            }
            return String.format(SAINT_SCORE_RESULT,
                    score.getScore(),
                    POS_DETAIL[saint.getPos()],
                    ratioString,
                    valueString,
                    saint.getPos() == 3 ? ("(指定元素伤害时：" + valueString2 + "体力)") : "",
                    score.getLevel()
            );
        } else {
            return String.format("得分：%.1f", saint.getSaintScore().getScore());
        }
    }

}
