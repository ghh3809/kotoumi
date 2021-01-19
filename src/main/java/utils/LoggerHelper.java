package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author guohaohao
 */
public class LoggerHelper {

    private static final Logger loggerInput = LoggerFactory.getLogger("Input");
    private static final Logger loggerOutput = LoggerFactory.getLogger("Output");

    /**
     * input log
     * @param sender 发送者信息
     * @param input input
     */
    public static void logInput(String sender, String input) {
        loggerInput.info("{} -> {}", sender, input);
    }

    /**
     * output log
     * @param receiver 接收者信息
     * @param output output
     */
    public static void logOutput(String receiver, String output) {
        loggerOutput.info("{} <- {}", receiver, output);
    }

    /**
     * 获取任意位随机ID
     * @return 随机ID
     */
    public static String getRandomID(int digits) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i ++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
