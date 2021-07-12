package entity.service;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author guohaohao
 */
@NoArgsConstructor
@AllArgsConstructor
public class DbSaint {

    private Long id;
    private Long userId;
    private String saintName;
    private Integer pos;
    private Integer level;
    private String score;
    private String ratio;
    private String mainProperty;
    private String subProperties;
    private String wishTime;
    private Integer enable;

    public DbSaint(Long userId, Saint saint) {
        this.userId = userId;
        this.saintName = saint.getName();
        this.pos = saint.getPos();
        this.level = saint.getLevel();
        this.score = String.format("%.6f", saint.getSaintScore().getScore());
        this.ratio = String.format("%.6f", saint.getSaintScore().getRatio());
        this.mainProperty = JSON.toJSONString(saint.getMainProperty());
        this.subProperties = JSON.toJSONString(saint.getSubProperties());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSaintName() {
        return saintName;
    }

    public void setSaintName(String saintName) {
        this.saintName = saintName;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public String getMainProperty() {
        return mainProperty;
    }

    public void setMainProperty(String mainProperty) {
        this.mainProperty = mainProperty;
    }

    public String getSubProperties() {
        return subProperties;
    }

    public void setSubProperties(String subProperties) {
        this.subProperties = subProperties;
    }

    public String getWishTime() {
        return wishTime;
    }

    public void setWishTime(String wishTime) {
        this.wishTime = wishTime;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }
}
