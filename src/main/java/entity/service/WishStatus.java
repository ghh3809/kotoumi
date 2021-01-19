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
     * 星辉总数
     */
    private int totalStarLight;

    public WishStatus() {
        this.star4Count = 0;
        this.star5Count = 0;
        this.totalStarLight = 0;
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
}
