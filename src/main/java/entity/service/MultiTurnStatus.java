package entity.service;

import constant.MultiTurnTask;

import java.util.List;

/**
 * @author guohaohao
 */
public class MultiTurnStatus {

    /**
     * 当前多轮任务
     */
    private MultiTurnTask multiTurnTask;
    /**
     * 待选卡牌列表
     */
    private List<Integer> cardNumbers;
    /**
     * 是否无框
     */
    private boolean noBox;
    /**
     * 是否觉醒
     */
    private boolean rankMax;
    /**
     * 技能等级
     */
    private int skillLevel;

    public MultiTurnTask getMultiTurnTask() {
        return multiTurnTask;
    }

    public void setMultiTurnTask(MultiTurnTask multiTurnTask) {
        this.multiTurnTask = multiTurnTask;
    }

    public List<Integer> getCardNumbers() {
        return cardNumbers;
    }

    public void setCardNumbers(List<Integer> cardNumbers) {
        this.cardNumbers = cardNumbers;
    }

    public boolean isNoBox() {
        return noBox;
    }

    public void setNoBox(boolean noBox) {
        this.noBox = noBox;
    }

    public boolean isRankMax() {
        return rankMax;
    }

    public void setRankMax(boolean rankMax) {
        this.rankMax = rankMax;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }
}
