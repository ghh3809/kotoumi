package processor;

import constant.BotInfo;
import constant.MessageSource;
import entity.service.QuerySignal;
import entity.service.Request;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.TempMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import utils.LoggerHelper;

import java.util.HashMap;

/**
 * @author guohaohao
 */
@Slf4j
public class Processor {

    public static Message process(MessageEvent messageEvent) {
        try {
            return processMessage(messageEvent);
        } catch (Exception e) {
            log.error("Process error", e);
            return null;
        }
    }

    /**
     * 处理消息事件
     * @param messageEvent 消息时间
     * @return 响应结果
     */
    public static Message processMessage(MessageEvent messageEvent) {

        // 消息来源bot
        Bot bot = messageEvent.getBot();

        // 消息发送者
        User sender = messageEvent.getSender();
        String senderString;
        if (sender instanceof Member) {
            Member member = (Member) sender;
            senderString = String.format("[%s(%d)]%s(%d)",
                    member.getGroup().getName(),
                    member.getGroup().getId(),
                    member.getNick(),
                    member.getId());
        } else {
            senderString = String.format("%s(%d)", sender.getNick(), sender.getId());
        }

        // 消息内容
        MessageChain messageChain = messageEvent.getMessage();
        QuerySignal querySignal = new QuerySignal();
        HashMap<String, Image> imageMap = new HashMap<>();
        String query = messageToString(messageChain, querySignal, imageMap);

        // 记录input日志
        LoggerHelper.logInput(senderString, query);
        log.info("Input: {} -> {}", senderString, query);

        // 消息处理工作
        MessageChain dialogResponse;
        if (messageEvent instanceof GroupMessageEvent && sender instanceof Member) {
            GroupMessageEvent groupMessageEvent = (GroupMessageEvent) messageEvent;
            MessageChain atMessage = new At((Member) sender).plus(" ");
            dialogResponse = DialogService.response(atMessage, new Request(
                    sender.getId(),
                    query,
                    MessageSource.GROUP,
                    groupMessageEvent.getGroup(),
                    bot,
                    messageChain,
                    querySignal,
                    imageMap
            ));
            if (dialogResponse == null) {
                return null;
            }
        } else if (messageEvent instanceof TempMessageEvent) {
            TempMessageEvent tempMessageEvent = (TempMessageEvent) messageEvent;
            dialogResponse = DialogService.response(null, new Request(
                    sender.getId(),
                    query,
                    MessageSource.TEMP,
                    tempMessageEvent.getGroup(),
                    bot,
                    messageChain,
                    querySignal,
                    imageMap
            ));
            if (dialogResponse == null) {
                return null;
            }
        } else if (messageEvent instanceof FriendMessageEvent) {
            dialogResponse = DialogService.response(null, new Request(
                    sender.getId(),
                    query,
                    MessageSource.FRIEND,
                    null,
                    bot,
                    messageChain,
                    querySignal,
                    imageMap
            ));
            if (dialogResponse == null) {
                return null;
            }
        } else {
            log.info("Ignore messageEvent: {}", messageEvent.getClass().getCanonicalName());
            return null;
        }

        // 记录日志
        String response = messageToString(dialogResponse, new QuerySignal(), null);
        LoggerHelper.logOutput(senderString, response);
        log.info("Output: {} <- {}", senderString, response);

        return dialogResponse;

    }

    /**
     * 将消息转化为string
     * @param messageChain 消息对象
     */
    private static String messageToString(MessageChain messageChain, QuerySignal querySignal, HashMap<String, Image> imageMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (SingleMessage message : messageChain) {
            if (message instanceof Image) {
                Image image = (Image) message;
                if (imageMap != null) {
                    imageMap.put(image.getImageId(), image);
                }
                stringBuilder.append("<image>").append(image.getImageId()).append("</image>");
            } else if (message instanceof At) {
                At at = (At) message;
                if (at.getTarget() == BotInfo.ID) {
                    querySignal.setAtMe(true);
                } else {
                    stringBuilder.append("<at>").append(at.getTarget()).append("</at>");
                }
            } else if (message instanceof Face) {
                Face face = (Face) message;
                stringBuilder.append("<face>").append(face.getId()).append("</face>");
            } else if (message instanceof PlainText) {
                PlainText plainText = (PlainText) message;
                stringBuilder.append(plainText.getContent());
            }
        }
        String query = stringBuilder.toString();
        if (query.contains(BotInfo.NAME)) {
            querySignal.setAtMe(true);
        }
        return query;
    }

}
