package processor;

import constant.MessageSource;
import constant.MultiTurnTask;
import dao.Dao;
import entity.service.Keyword;
import entity.service.MultiTurnStatus;
import entity.service.Request;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.EmptyMessageChain;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.apache.commons.lang3.StringUtils;
import processor.dialogue.SaintDialogService;
import processor.dialogue.SifDialogService;
import processor.dialogue.SystemDialogService;
import processor.dialogue.UtilDialogService;
import processor.dialogue.WishDialogService;
import utils.ConfigHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guohaohao
 */
@Slf4j
public class DialogService {

    public static final ConcurrentHashMap<String, MultiTurnStatus> MULTI_TURN_STATUS_MAP = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Long, HashMap<String, List<Keyword>>> KEYWORD_MAP = new ConcurrentHashMap<>();

    /**
     * query正则表达式
     */
    private static final Pattern CHOICE_PATTERN = Pattern.compile("^\\d$");
    private static final Pattern KEYWORD_ADD_PATTERN = Pattern.compile("^问[ _](.+?)[ _]答[ _](.+)$", Pattern.DOTALL);
    private static final Pattern WISH_PATTERN = Pattern.compile("^(普通|快速|无图|)(抽卡|单抽|10连|十连)(.+?)池.*$");
    private static final Pattern WISH_SAINT_PATTERN = Pattern.compile("^(抽|白嫖)圣遗物(.*)$");
    private static final Pattern STRENGTH_SAINT_PATTERN = Pattern.compile("^强化圣遗物([0-9]+).*$");
    private static final Pattern FIND_SAINT_PATTERN = Pattern.compile("^查看圣遗物([0-9]+).*$");
    private static final Pattern ADD_PRIMOGEMS_PATTERN = Pattern.compile("^氪金_(.+?)_(.+)$");
    private static final Pattern SIF_RANK_PATTERN = Pattern.compile("^(国服|当前|实时)?档线$");
    private static final Pattern WISH_RESULT_PATTERN = Pattern.compile("^我的(圣遗物|统计|角色|武器|群友|奖品)$");
    private static final Pattern MODE_SET_PATTERN = Pattern.compile("^设置图片模式(普通|快速|无图)$");
    /**
     * query关键字
     */
    private static final String KLEE_KEYWORD = "可莉";
    private static final String HELP_KEYWORD = "帮助";
    private static final String GENSHIN_KEYWORD = "原神";
    private static final String QUERY_CARD_KEYWORD = "查卡";
    private static final String KEYWORD_QUERY_KEYWORD = "查询词库";
    private static final String KEYWORD_FUZZY_QUERY_KEYWORD = "模糊查询词库";
    private static final String KEYWORD_FUZZY_QUERY_ANSWER = "模糊查询答案";
    private static final String KEYWORD_DELETE_KEYWORD = "删除词库";
    private static final String SIGN_IN_KEYWORD = "签到";
    private static final String ADD_SIGN_IN_KEYWORD = "补签";
    private static final String DRAW_KEYWORD = "抽签";
    private static final String DIVINE_KEYWORD = "占卜";
    private static final String SUBSTITUTE_KEYWORD = "设置替换关键词";
    private static final String CARD_PK_KEYWORD = "卡组pk";
    private static final String TRANSFORM_KEYWORD = "星辉全部换原石";
    private static final String PROB_KEYWORD = "概率说明";
    private static final String CURRENT_WISH_KEYWORD = "当前卡池";
    private static final String SAINT_SCORE_KEYWORD1 = "圣遗物评分";
    private static final String SAINT_SCORE_KEYWORD2 = "圣遗物分数";
    private static final String SAINT_RANK_KEYWORD = "圣遗物排名";

    private static final String[] DEFAULT_REPLY = new String[] {
            "西风骑士团「火花骑士」，可莉，前来报到！…呃——后面该说什么词来着？可莉背不下来啦…",
            "早安！带可莉出去玩吧！我们一起来冒险！",
            "午饭时间到了！吃什么呢？我来看看《提瓦特游览指南》…",
            "晚上好！拜托你也帮我跟霍夫曼先生说说吧，可莉不是小孩子了，可莉晚上也可以出去玩。带我出去玩——",
            "虽然出来玩的时候说不想回家，但天黑以后的旷野，不认识路…拜、拜托你回去的时候，把我也送回家好不好…",
            "可莉今天又勇敢地抓到了花纹奇怪的蜥蜴！从没见过这种图案，你要看看吗？",
            "要和可莉一起去炸鱼吗？虽然被抓住就是一整天的禁闭，但鱼很好吃，所以值得！",
            "「城里放炮禁闭室报道」、「炸弹伤人琴找上门」、「放火烧山可莉完蛋」——这就是可莉在骑士团的生存守则。",
            "哼哼哼，这次的炸弹可是防水的。",
            "什么东西爆炸了…？！哦，是打雷啊。",
            "风神保佑，把可莉的炸弹往合适的地方吹吧。",
            "呜…这次居然炸歪了大风车的叶片…真对不起！",
            "我，我还挺会做手工的，请让我帮你们重新做一个…什么？禁止雇佣童工？呜呃…",
            "不行！才九点，可莉今晚一定要撑到看完马戏团的午夜专场！你是可莉的玩伴吧！那就别想现在就把我送回家，略略略略…",
            "是自由吗？那以后要不要更自由地出去炸鱼，来回应巴巴托斯大人的期待呢…？",
            "可莉知道，骑士团的墙是不能炸的。虽然打通了能从禁闭室出去很方便，但凯亚哥哥说，这样做了以后，琴团长恐怕会让我再也见不到第二天的太阳…",
            "每一只蜥蜴的花纹都是不同的，有些蜥蜴的尾巴干燥以后磨成粉，可以当做炸药的材料哦。这是阿贝多哥哥告诉我的事。",
            "我们是朋友！雷泽努力对抗危机、保护可莉的样子，让人安心！唔…虽然基本上都是我把危机带到他身边的…",
            "爸爸和妈妈都是全大陆有名的旅行家！妈妈写的《提瓦特游览指南》，有好多餐馆和旅店都很在意它的权威评价呢。",
            "凯亚哥哥是好人！《骑士团生存守则》就是凯亚哥哥帮可莉写的。可莉闯祸的时候，他也经常帮忙遮掩呢。",
            "安柏姐姐是好人！兔兔伯爵摸起来也是毛茸茸的，而且还会爆炸，太棒了！",
            "琴团长是好人！虽然…有点可怕…",
            "奇怪的大人，一直都是不太开心的样子。唔…为什么不喜欢笑呢？",
            "丽莎阿…丽莎姐姐是好人！每次可莉靠近她的工坊，她就会给可莉好吃的茶点…欸？说起来我想进她的工坊，好像一开始是想做新的炸药来着…？",
            "虽然班尼特哥哥自己总是什么宝物都找不到，嗯…这是为什么呢？",
            "迪奥娜姐姐是好人！「靠一己之力摧毁蒙德酒业」的想法好有趣，可莉也要用炸弹来帮忙！呃，虽然不太懂什么叫做「蒙德酒业」，是怪物吗？",
            "阿贝多哥哥跟可莉有个秘密的约定!要是可莉看到工坊门前挂着「实验进行中」的牌子，就要晚点再去找他。阿贝多哥哥不工作的时候，会很耐心地陪着可莉，所以可莉也会很耐心地等着的!",
            "莫娜姐姐是好人!她每次找阿贝多哥哥和可莉玩的时候，都会说好多听不明白，但是很有趣的东西!可莉喜欢她来玩!",
            "砂糖姐姐是好人!她问我，想不想要一个会跑会跳的蹦蹦! 嘿嘿嘿，当然想要了,我还想要好几百个，让蒙德的街道上，台阶上，广场上，全是蹦蹦!",
            "你好！你是来找可莉玩的吗？",
            "是啊，这个炸弹是可莉最新改良的作品！你看我做得好不好？…呃——如果是在什么纵火现场发现的话，那就不是我做的，绝对不是。",
            "虽然新炸药的配方，很多都是在被关禁闭的时候想出来的…但如果不被关禁闭的话就更好了…",
            "这是嘟嘟可，是可莉很久以前就交到的好朋友。要记得它的名字哦，以后别叫它「挂在你包上的玩偶」啦。",
            "谢谢你总是帮可莉解决麻烦！就用这串「可莉烤鱼」来报答你吧！呃，其实鱼是直接在湖里就被炸弹烤熟了，不过这件事就和平时一样，假装不知道好了。",
            "可莉喜欢毛茸茸的东西。比如嘟嘟可、蒲公英，还有雷泽的头发。",
            "唉，你是好人，我是坏孩子…等我这次的禁闭结束、好好反省过以后，再来找你带我出去玩…",
            "蒙德的鲈鱼是很好吃的，从果酒湖里抓到的尤其好吃！这可不是瞎说的，连妈妈也认同可莉的想法喔。",
            "讨厌蒙德蟹——什么蟹都讨厌——可莉要出去玩——不要坐在餐桌前慢慢剥壳——",
            "生日快乐!过生日很开心，你比可莉大，那过生日的次数一定比可莉多吧? 可莉很羡慕!",
            "嗯，新型炸弹研究完毕！趁琴团长发现之前，开溜！",
            "又进步了一点！不过琴团长总是说：进步是分内之事。",
            "谢谢你总是陪我一起收集研究用的材料！如果你想学习炸弹的制作方法，我也可以来教你哦。",
            "很久以前，第一次听到爆炸声的时候，我没有害怕…后来我才知道，大部分人不是这样的…不过，你也不害怕我的炸弹呢！嘿嘿，太好了，可莉果然交到了很好的朋友！",
            "蹦蹦炸弹！",
            "弹起来吧！",
            "嘿（姆）咻！",
            "轰轰火花！",
            "火力全开！",
            "全——都可以炸完！",
            "啦啦啦~",
            "哒哒哒~",
            "可莉又找到新的宝物了！",
            "亮闪闪的，好开心！",
            "地下会不会埋着更多宝箱呢…",
            "可莉来帮忙！",
            "锵锵—可莉登场。",
            "好耶！是大冒险。"
    };

    private static final Random RANDOM = new Random();

    static {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> Dao.getSaint(1), 1, 1, TimeUnit.MINUTES);
    }

    public static MessageChain response(MessageChainBuilder at, Request request) {
        ResponseFlag responseFlag = new ResponseFlag();
        MessageChain messageChain = response(request, responseFlag);
        if (messageChain != null) {
            if (at != null && responseFlag.needAt) {
                return at.append(messageChain).build();
            } else {
                return messageChain;
            }
        } else {
            return null;
        }
    }

    /**
     * 根据用户请求，进行回复
     * @param request 用户请求
     * @return 回复信息
     */
    public static MessageChain response(Request request, ResponseFlag responseFlag) {

        // 预处理
        request.setQuery(request.getQuery().trim());
        String userId = getUserId(request);

        // 当前是否有多轮任务进行中
        if (MULTI_TURN_STATUS_MAP.containsKey(userId)) {
            Matcher choiceMatcher = CHOICE_PATTERN.matcher(request.getQuery());
            if (choiceMatcher.find()) {
                log.info("Choice query found");
                int choice = Integer.parseInt(request.getQuery());
                MultiTurnStatus multiTurnStatus = MULTI_TURN_STATUS_MAP.get(userId);
                if (multiTurnStatus.getMultiTurnTask().equals(MultiTurnTask.QUERY_CARD)) {
                    // 查卡多轮会话
                    if (choice < 1 || choice > multiTurnStatus.getCardNumbers().size()) {
                        return EmptyMessageChain.INSTANCE.plus("请直接回复序号进行选择");
                    } else {
                        return SifDialogService.responseCard(multiTurnStatus.getCardNumbers().get(choice - 1),
                                multiTurnStatus.isNoBox(),
                                multiTurnStatus.isRankMax(),
                                multiTurnStatus.getSkillLevel(),
                                request);
                    }
                }
            } else {
                MULTI_TURN_STATUS_MAP.remove(userId);
            }
        }

        // 搜索内置关键字
        Matcher rankMatcher = SIF_RANK_PATTERN.matcher(request.getQuery());
        Matcher addKeywordMatcher = KEYWORD_ADD_PATTERN.matcher(request.getQuery());
        Matcher wishMatcher = WISH_PATTERN.matcher(request.getQuery());
        Matcher addPrimogemsMatcher = ADD_PRIMOGEMS_PATTERN.matcher(request.getQuery());
        Matcher wishResultMatcher = WISH_RESULT_PATTERN.matcher(request.getQuery());
        Matcher modeSetMatcher = MODE_SET_PATTERN.matcher(request.getQuery());
        Matcher wishSaintMatcher = WISH_SAINT_PATTERN.matcher(request.getQuery());
        Matcher strengthSaintMatcher = STRENGTH_SAINT_PATTERN.matcher(request.getQuery());
        Matcher findSaintMatcher = FIND_SAINT_PATTERN.matcher(request.getQuery());
        if (request.getQuery().equals(HELP_KEYWORD) || request.getQuery().equals(KLEE_KEYWORD)) {
            // 帮助
            return SystemDialogService.help();
        } else if (request.getQuery().equals(GENSHIN_KEYWORD)) {
            // 签到
            return SystemDialogService.genshin();
        } else if (request.getQuery().equals(SIGN_IN_KEYWORD)) {
            // 签到
            return SystemDialogService.signIn(request);
        } else if (request.getQuery().equals(DRAW_KEYWORD)) {
            // 抽签
            return SystemDialogService.draw(request);
        } else if (request.getQuery().equals(DIVINE_KEYWORD)) {
            // 占卜
            return SystemDialogService.divine(request);
        } else if (request.getQuery().startsWith(QUERY_CARD_KEYWORD)) {
            // 查卡
            return SifDialogService.queryCard(request);
        } else if (request.getQuery().startsWith(KEYWORD_QUERY_KEYWORD)) {
            // 查询词库
            return SystemDialogService.queryKeyword(request, 0);
        } else if (request.getQuery().startsWith(KEYWORD_FUZZY_QUERY_KEYWORD)) {
            // 查询词库
            return SystemDialogService.queryKeyword(request, 1);
        } else if (request.getQuery().startsWith(KEYWORD_FUZZY_QUERY_ANSWER)) {
            // 查询词库
            return SystemDialogService.queryKeyword(request, 2);
        } else if (request.getQuery().startsWith(KEYWORD_DELETE_KEYWORD)) {
            // 删除词库
            return SystemDialogService.deleteKeyword(request);
        } else if (request.getQuery().startsWith(SUBSTITUTE_KEYWORD)) {
            // 设置替换关键词
            return SystemDialogService.substitute(request);
        } else if (wishMatcher.find()) {
            // 抽卡
            return WishDialogService.wish(request, wishMatcher);
        } else if (request.getQuery().startsWith(CARD_PK_KEYWORD)) {
            // 卡组pk
            return SystemDialogService.cardPk(request);
        } else if (request.getQuery().startsWith(ADD_SIGN_IN_KEYWORD)) {
            // 补签
            return SystemDialogService.addSignIn(request);
        } else if (addKeywordMatcher.find()) {
            // 添加词库
            return SystemDialogService.addKeyword(request, addKeywordMatcher);
        } else if (wishResultMatcher.find()) {
            // 我的祈愿信息
            return WishDialogService.myWish(request, wishResultMatcher);
        } else if (request.getQuery().equals(TRANSFORM_KEYWORD)) {
            // 星辉全部换原石
            return WishDialogService.transform(request);
        } else if (request.getQuery().equals(PROB_KEYWORD)) {
            // 概率说明
            return WishDialogService.prob(request);
        } else if (addPrimogemsMatcher.find()) {
            // 增加原石
            return WishDialogService.addPrimogems(request, addPrimogemsMatcher);
        } else if (request.getQuery().equals(CURRENT_WISH_KEYWORD)) {
            // 当前卡池
            return WishDialogService.currentWish(request);
        } else if (rankMatcher.find()) {
            // 国服档线
            return SifDialogService.sifRank(request);
        } else if (modeSetMatcher.find()) {
            // 设置招募模式
            return WishDialogService.setMode(request, modeSetMatcher);
        } else if ((request.getQuery().contains(SAINT_SCORE_KEYWORD1) || request.getQuery().contains(SAINT_SCORE_KEYWORD2)) &&
                !request.getQuery().contains("#") && !request.getQuery().contains("＃")) {
            // 圣遗物评分
            return SaintDialogService.saintScore(request);
        } else if (wishSaintMatcher.find()) {
            // 抽圣遗物
            return SaintDialogService.wish(request, wishSaintMatcher);
        } else if (strengthSaintMatcher.find()) {
            // 强化圣遗物
            return SaintDialogService.strength(request, strengthSaintMatcher);
        } else if (findSaintMatcher.find()) {
            // 查看圣遗物
            return SaintDialogService.find(request, findSaintMatcher);
        } else if (request.getQuery().equals(SAINT_RANK_KEYWORD)) {
            // 群圣遗物排名
            return SaintDialogService.groupRank(request);
        }

        // 群自定义词库
        if (request.getMessageSource().equals(MessageSource.GROUP)) {
            if (!KEYWORD_MAP.containsKey(request.getGroup().getId())) {
                KEYWORD_MAP.put(request.getGroup().getId(), SystemDialogService.buildKeywordMap(request.getGroup().getId()));
            }
            List<Keyword> keywordList = KEYWORD_MAP.get(request.getGroup().getId()).get(request.getQuery());
            if (keywordList != null && !keywordList.isEmpty()) {
                Keyword keyword = keywordList.get(RANDOM.nextInt(keywordList.size()));
                log.info("Keyword query found: {}", keyword.getId());
                responseFlag.needAt = false;
                return UtilDialogService.responsePattern(keyword.getResponse(), request);
            }
        }

        // 均不满足时的回复
        return defaultResponse(request);
    }

    /**
     * 默认回复
     * @param request 用户请求
     * @return 返回
     */
    private static MessageChain defaultResponse(Request request) {
        if (!request.getMessageSource().equals(MessageSource.GROUP) || request.getQuerySignal().isAtMe()) {
            if (StringUtils.isBlank(request.getQuery())) {
                return EmptyMessageChain.INSTANCE.plus(DEFAULT_REPLY[RANDOM.nextInt(DEFAULT_REPLY.length)]);
            }
            String response;
            if (request.getFrom() == ConfigHelper.ADMIN_QQ) {
                response = ChatGPTService.dialog(getUserId(request), request.getQuery());
            } else {
                response = UnitService.dialog(getUserId(request), request.getQuery());
            }
            if (response != null) {
                return EmptyMessageChain.INSTANCE.plus(response);
            } else {
                return EmptyMessageChain.INSTANCE.plus("这个问题我现在还不会，你可以用\"问_{问题}_答_{答案}\"来教我哦！");
            }
        }
        return null;
    }

    /**
     * 获取用户唯一标识
     * @param request 请求信息
     * @return 用户标识
     */
    public static String getUserId(Request request) {
        if (request.getMessageSource().equals(MessageSource.GROUP)) {
            return request.getGroup().getId() + ":" + request.getFrom();
        } else {
            return String.valueOf(request.getFrom());
        }
    }

    private static class ResponseFlag {
        private boolean needAt;
        ResponseFlag() {
            needAt = true;
        }
    }

}
