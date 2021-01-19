package entity.service;

/**
 * @author guohaohao
 */
public class Daily {

    /**
     * 词条ID
     */
    private Long id;
    /**
     * 群ID
     */
    private Long groupId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 签到结果
     */
    private Integer signInResult;
    /**
     * 抽签结果
     */
    private Integer drawResult;
    /**
     * 占卜结果
     */
    private Integer divineResult;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 修改时间
     */
    private String updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSignInResult() {
        return signInResult;
    }

    public void setSignInResult(Integer signInResult) {
        this.signInResult = signInResult;
    }

    public Integer getDrawResult() {
        return drawResult;
    }

    public void setDrawResult(Integer drawResult) {
        this.drawResult = drawResult;
    }

    public Integer getDivineResult() {
        return divineResult;
    }

    public void setDivineResult(Integer divineResult) {
        this.divineResult = divineResult;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
