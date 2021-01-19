package entity.service;

/**
 * @author guohaohao
 */
public class QuerySignal {

    /**
     * 是否在at机器人
     */
    private boolean atMe;

    public QuerySignal() {
        atMe = false;
    }

    public boolean isAtMe() {
        return atMe;
    }

    public void setAtMe(boolean atMe) {
        this.atMe = atMe;
    }
}
