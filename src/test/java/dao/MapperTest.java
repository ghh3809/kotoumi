package dao;

import com.alibaba.fastjson.JSON;
import entity.service.Keyword;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.junit.Test;
import processor.DialogService;
import processor.dialogue.UtilDialogService;
import utils.FileHelper;
import utils.RequestHelper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MapperTest {

    @Test
    public void testFindUnitByNumber() {
        log.info(JSON.toJSONString(Dao.findUnitByNumber(769)));
    }

    @Test
    public void testFindUnitByName() {
        log.info(JSON.toJSONString(Dao.findUnitByName("旗袍", "旗袍", null)));
    }

    @Test
    public void testInsertKeyword() {
        Keyword keyword = new Keyword();
        keyword.setGroupId(199188177L);
        keyword.setCreatorId(1020304050L);
        keyword.setKeyword("123");
        keyword.setResponse("456");
        Dao.addKeyword(keyword);
        long id = Dao.getId(199188177L);
        log.info(JSON.toJSONString(Dao.findKeywords(199188177L)));
        log.info(JSON.toJSONString(Dao.findKeywordById(199188177L, id)));
        log.info(JSON.toJSONString(Dao.findKeywordByKey(199188177L, "", 0)));
        log.info(JSON.toJSONString(Dao.findKeywordByKey(199188177L, "123", 0)));
        Dao.deleteKeyword(199188177L, id);
    }

    @Test
    public void testDaily() {
        Dao.addDaily(199188177L, 1020304050L);
        Dao.updateDailySignIn(199188177L, 1020304050L, "2020-11-09", 3);
        Dao.updateDailyDivine(199188177L, 1020304050L, "2020-11-09", 4);
        Dao.updateDailyDraw(199188177L, 1020304050L, "2020-11-09", 2);
        log.info(JSON.toJSONString(Dao.findDailyById(199188177L, 1020304050L, "2020-11-09")));
    }

    @Test
    public void testSplitMessage() {
        List<String> messageList = UtilDialogService.splitMessage("123</at>ahshdf<at>张三</at>abc<image>图片01</image>def<at>李四</at>");
        for (String message : messageList) {
            log.info(message);
        }
    }

    @Test
    public void testRegex() {
        Pattern pattern = Pattern.compile("^问[ _](.+?)[ _]答[ _](.+)$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher("问 1\n2 答 2\n3\n4\n5");
        if (matcher.find()) {
            log.info(matcher.group(1));
            log.info(matcher.group(2));
        }
    }

    @Test
    public void testFile() {
        File file = new File("./test");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("cookie", "uiLocalize=zh-cn; dbLocalize=CN");
        String response = RequestHelper.httpGet(String.format("https://card.niconi.co.ni/cardApi/%d", 378), headers);
        FileHelper.saveToFile(file, Collections.singletonList(response), false);
        String result = Objects.requireNonNull(FileHelper.readLines(file)).get(0).trim();
        log.info(JSON.parseObject(result).getJSONArray("skill_level").getJSONObject(8).getString("description"));
        log.info(JSON.toJSONString(FileHelper.readLines(file)));
    }

}
