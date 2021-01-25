package entity.service;

/**
 * @author guohaohao
 */
public class WishStatus {

    /**
     * 上一个4星以后又抽的数量
     */
    private int star4Count;
    /**
     * 上一个5星以后又抽的数量
     */
    private int star5Count;
    /**
     * 是否下一个必为up
     */
    private boolean mustUp;
    /**
     * 星辉总数
     */
    private int totalStarLight;
    /**
     * 五星保底需要的抽数
     */
    private int maxFiveCount;

    public WishStatus() {
        this.star4Count = 0;
        this.star5Count = 0;
        this.mustUp = false;
        this.totalStarLight = 0;
        this.maxFiveCount = 90;
    }

    public int getStar4Count() {
        return star4Count;
    }

    public void setStar4Count(int star4Count) {
        this.star4Count = star4Count;
    }

    public int getStar5Count() {
        return star5Count;
    }

    public void setStar5Count(int star5Count) {
        this.star5Count = star5Count;
    }

    public int getTotalStarLight() {
        return totalStarLight;
    }

    public void setTotalStarLight(int totalStarLight) {
        this.totalStarLight = totalStarLight;
    }

    public boolean isMustUp() {
        return mustUp;
    }

    public void setMustUp(boolean mustUp) {
        this.mustUp = mustUp;
    }

    public int getMaxFiveCount() {
        return maxFiveCount;
    }

    public void setMaxFiveCount(int maxFiveCount) {
        this.maxFiveCount = maxFiveCount;
    }
}
