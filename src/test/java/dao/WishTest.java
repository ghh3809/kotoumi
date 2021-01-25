package dao;

import com.alibaba.fastjson.JSON;
import entity.service.Keyword;
import entity.service.WishStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import processor.DialogService;
import utils.FileHelper;
import utils.RequestHelper;
import utils.WishHelper;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WishTest {

    @Test
    public void testWish() {
        Random random = new Random();
        WishStatus wishStatus = new WishStatus();
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int i = 0; i < 90; i ++) {
            countMap.put(i + 1, 0);
        }
        int count5 = 0, count4 = 0, total = 0;
        for (int i = 0; i < 100000000; i ++) {
            double prob4 = WishHelper.getNextProb4(wishStatus, 2);
            double prob5 = WishHelper.getNextProb5(wishStatus, 2);
            double current = random.nextDouble();
            total ++;
//            log.info("prob5: {}, prob4: {}, current: {}", prob5, prob4, current);
            if (current < prob5) {
//                log.info("Get 5");
                int times = wishStatus.getStar5Count() + 1;
                countMap.put(times, countMap.get(times) + 1);
                wishStatus.setStar4Count(0);
                wishStatus.setStar5Count(0);
                count5 ++;
            } else if (current < prob5 + prob4) {
//                log.info("Get 4");
                wishStatus.setStar4Count(0);
                wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
                count4 ++;
            } else {
                wishStatus.setStar4Count(wishStatus.getStar4Count() + 1);
                wishStatus.setStar5Count(wishStatus.getStar5Count() + 1);
            }
        }
        log.info("total: {}, count5: {}, count4: {}", total, count5, count4);
        for (int i = 0; i < 90; i ++) {
            log.info("Times = " + (i + 1) + ", Count = " + countMap.get(i + 1));
        }
    }

}
