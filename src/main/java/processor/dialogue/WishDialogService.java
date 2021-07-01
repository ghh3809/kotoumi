package processor.dialogue;

import com.alibaba.fastjson.JSON;
import constant.UnitType;
import dao.Dao;
import entity.service.GenshinUnit;
import entity.service.PrimoGems;
import entity.service.Request;
import entity.service.WishEvent;
import entity.service.WishStatus;
import entity.service.WishSummary;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.MessageChain;
import utils.WishHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author guohaohao
 */
@Slf4j
public class WishDialogService {

    private static final long ADMIN_QQ = 1146875163L;
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
    private static final String GENSHIN_PIC_DIR = "./pics/genshin/";

    /**
     * 抽卡
     * @param request 请求
     * @return 返回
     */
    public static MessageChain wish(Request request, Matcher matcher) {

        log.info("Wish found");
        // 预处理
        int wishMode = 0;
        String wishModeStr = matcher.group(1);
        String wishCountStr = matcher.group(2);
        int wishCount = 10;
        if ("快速".equals(wishModeStr)) {
            wishMode = 1;
        } else if ("无图".equals(wishModeStr)) {
            wishMode = 2;
        } else if ("".equals(wishModeStr)) {
            Integer dbWishMode = Dao.getWishMode(request.getFrom());
            if (dbWishMode != null) {
                wishMode = dbWishMode;
            }
        }
        if ("抽卡".equals(wishCountStr) || "单抽".equals(wishCountStr)) {
            wishCount = 1;
        }
        log.info("wishMode: {}, wishCount: {}", wishMode, wishCount);
        String wishTypeStr = matcher.group(3);

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
        Dao.addPrimogems(request.getFrom(), - wishCount * 160, wishStatus.getTotalStarLight(), 0);
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
            }
            if ((wishMode == 0 && genshinUnit.getRarity() > 3) ||
                    (wishMode == 1 && genshinUnit.getRarity() > 4)) {
                picUnits.add(genshinUnit.getId());
            }

            // 生成抽卡结果信息
            if (wishCount == 1 || genshinUnit.getRarity() > 3) {
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
        }
        MessageChain messageChain = EmptyMessageChain.INSTANCE.plus(stringBuilder);

        // 图片展示
        for (Long id : picUnits) {
            File file = new File(GENSHIN_PIC_DIR + id + ".png");
            if (file.exists()) {
                messageChain = messageChain.plus(UtilDialogService.uploadImage(request, file)).plus("\n");
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
     * 查看自己的祈愿信息
     * @param request 请求信息
     * @param matcher 正则
     * @return 返回
     */
    public static MessageChain myWish(Request request, Matcher matcher) {

        log.info("My wish found");
        String keyword = matcher.group(1);
        if ("圣遗物".equals(keyword)) {
            return SaintDialogService.mySaint(request);
        } else if ("统计".equals(keyword)) {
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
    public static MessageChain transform(Request request) {
        log.info("Transform found");
        PrimoGems primoGems = Dao.getPrimogems(request.getFrom());
        log.info("Current status: {}", JSON.toJSONString(primoGems));
        if (primoGems.getStarlight() < 5) {
            return EmptyMessageChain.INSTANCE.plus("星辉数量不足，当前星辉数量：" + primoGems.getStarlight());
        }
        int times = primoGems.getStarlight() / 5;
        Dao.addPrimogems(request.getFrom(), times * 160, - times * 5, 0);
        return EmptyMessageChain.INSTANCE.plus("兑换成功！当前星辉数量：" + (primoGems.getStarlight() - times * 5)
                + "，当前原石数量：" + (primoGems.getPrimogems() + times * 160));
    }

    /**
     * 概率说明
     * @param request 请求
     * @return 返回
     */
    public static MessageChain prob(Request request) {
        log.info("Prob found");
        return EmptyMessageChain.INSTANCE.plus(PROB_RESPONSE);
    }

    /**
     * 氪金
     * @param request 请求
     * @return 返回
     */
    public static MessageChain addPrimogems(Request request, Matcher matcher) {
        log.info("Add Primogems found");
        if (request.getFrom() != ADMIN_QQ) {
            return EmptyMessageChain.INSTANCE.plus("无权访问！");
        }
        long userId = Long.parseLong(matcher.group(1));
        int amount = Integer.parseInt(matcher.group(2));
        Dao.addPrimogems(userId, amount, 0, 0);
        int current = Dao.getPrimogems(userId).getPrimogems();
        return EmptyMessageChain.INSTANCE.plus("氪金成功！账号：" + userId + ", 添加原石数：" + amount
                + ", 剩余原石数：" + current);
    }

    /**
     * 查看当前开放的卡池
     * @param request 请求
     * @return 返回
     */
    public static MessageChain currentWish(Request request) {
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
    public static MessageChain mySummary(Request request) {
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
     * 设置抽卡图片模式
     * @param request 请求
     * @param matcher matcher
     * @return 响应
     */
    public static MessageChain setMode(Request request, Matcher matcher) {
        log.info("Set mode found");
        String modeString = matcher.group(1);
        int mode = 0;
        if ("快速".equals(modeString)) {
            mode = 1;
        } else if ("无图".equals(modeString)) {
            mode = 2;
        }
        if (Dao.getWishMode(request.getFrom()) == null) {
            Dao.addWishMode(request.getFrom(), mode);
        } else {
            Dao.updateWishMode(request.getFrom(), mode);
        }
        return EmptyMessageChain.INSTANCE.plus("设置成功！当前抽卡模式为：" + modeString);
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

}
