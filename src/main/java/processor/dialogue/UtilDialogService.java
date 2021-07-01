package processor.dialogue;

import constant.MessageSource;
import entity.service.Request;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.MessageChain;
import processor.DialogService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guohaohao
 */
@Slf4j
public class UtilDialogService {

    private static final String CHAT_PIC_DIR = "./pics/chat/";
    private static boolean IMAGE_FLAG = false;

    /**
     * 上传图片
     * @param request 请求
     * @param image 上传后的图片文件
     * @return messageChain
     */
    public static MessageChain uploadImage(Request request, File image) {
        boolean accept;
        synchronized (DialogService.class) {
            if (! IMAGE_FLAG) {
                IMAGE_FLAG = true;
                accept = true;
            } else {
                accept = false;
            }
        }
        if (!accept) {
            return EmptyMessageChain.INSTANCE.plus("[图片]");
        }
        try {
            if (request.getMessageSource().equals(MessageSource.GROUP)) {
                // 备注：虽然不知道为什么这里要上传两遍，但是少任意一个都不能用……
                // UPDATE: 好像不加这句可以用了，诶嘿
                // request.getGroup().getBotAsMember().uploadImage(image);
                return EmptyMessageChain.INSTANCE.plus(request.getGroup().uploadImage(image));
            } else {
                return EmptyMessageChain.INSTANCE.plus(request.getBot().getSelfQQ().uploadImage(image));
            }
        } finally {
            synchronized (DialogService.class) {
                IMAGE_FLAG = false;
            }
        }

    }

    /**
     * 响应keyword模板格式
     * @param response 响应模板
     * @return 返回
     */
    public static MessageChain responsePattern(String response, Request request) {
        // 初始化
        MessageChain messageChain = EmptyMessageChain.INSTANCE;
        List<String> messageList = splitMessage(response);
        for (String message : messageList) {
            String tag = checkTag(message);
            if (tag == null) {
                messageChain = messageChain.plus(message);
            } else if ("at".equals(tag)) {
                long id = Long.parseLong(message.substring(4, message.length() - 5));
                messageChain = messageChain.plus(new At(request.getGroup().get(id)));
            } else if ("face".equals(tag)) {
                int id = Integer.parseInt(message.substring(6, message.length() - 7));
                messageChain = messageChain.plus(new Face(id));
            } else if ("image".equals(tag)) {
                String id = message.substring(7, message.length() - 8);
                messageChain = messageChain.plus(UtilDialogService.uploadImage(request, new File(CHAT_PIC_DIR + id)));
            }
        }
        return messageChain;
    }

    /**
     * 将消息划分为tag段
     * @param content 消息体
     * @return tag段
     */
    public static List<String> splitMessage(String content) {
        // 初始化
        List<String> messageList = new ArrayList<>();
        messageList.add(content);

        // 遍历所有的消息，分割tag
        String[] validTags = new String[] {"at", "face", "image"};
        for (String tag : validTags) {
            List<String> newMessageList = new ArrayList<>();
            for (String message : messageList) {
                // 对于非划分好的消息，进行分割
                if (checkTag(message) == null) {
                    String[] messagePart1 = message.split("<" + tag + ">");
                    newMessageList.add(messagePart1[0]);
                    for (int i = 1; i < messagePart1.length; i ++) {
                        // 最多划分两份
                        String[] messagePart2 = messagePart1[i].split("</" + tag + ">", 2);
                        if (messagePart2.length > 1) {
                            newMessageList.add("<" + tag + ">" + messagePart2[0] + "</" + tag + ">");
                            if (messagePart2[1].length() > 0) {
                                newMessageList.add(messagePart2[1]);
                            }
                        } else {
                            newMessageList.add("<" + tag + ">" + messagePart1[i]);
                        }
                    }
                } else {
                    newMessageList.add(message);
                }
            }
            messageList = newMessageList;
        }
        return messageList;
    }

    /**
     * 检查一段文字是否被tag包围，如果是则返回tag
     * @param content 文本
     * @return tag，没有时为null
     */
    private static String checkTag(String content) {
        String[] validTags = new String[] {"at", "face", "image"};
        for (String tag : validTags) {
            if (content.startsWith(String.format("<%s>", tag)) && content.endsWith(String.format("</%s>", tag))) {
                return tag;
            }
        }
        return null;
    }

}
