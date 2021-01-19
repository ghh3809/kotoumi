package entity.service;

import constant.MessageSource;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashMap;

/**
 * @author guohaohao
 */
public class Request {

    /**
     * 消息来源者ID
     */
    private long from;
    /**
     * 转义后的query
     */
    private String query;
    /**
     * 消息来源
     */
    private MessageSource messageSource;
    /**
     * 消息群组
     */
    private Group group;
    /**
     * 消息bot
     */
    private Bot bot;
    /**
     * 消息内容
     */
    private MessageChain messageChain;
    /**
     * 消息信号
     */
    private QuerySignal querySignal;
    /**
     * 图片映射
     */
    private HashMap<String, Image> imageMap;

    public Request(long from, String query, MessageSource messageSource, Group group, Bot bot, MessageChain messageChain, QuerySignal querySignal, HashMap<String, Image> imageMap) {
        this.from = from;
        this.query = query;
        this.messageSource = messageSource;
        this.group = group;
        this.bot = bot;
        this.messageChain = messageChain;
        this.querySignal = querySignal;
        this.imageMap = imageMap;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public MessageChain getMessageChain() {
        return messageChain;
    }

    public void setMessageChain(MessageChain messageChain) {
        this.messageChain = messageChain;
    }

    public QuerySignal getQuerySignal() {
        return querySignal;
    }

    public void setQuerySignal(QuerySignal querySignal) {
        this.querySignal = querySignal;
    }

    public HashMap<String, Image> getImageMap() {
        return imageMap;
    }

    public void setImageMap(HashMap<String, Image> imageMap) {
        this.imageMap = imageMap;
    }

}
