package com.liyuhua.seckill.utils;
import java.util.UUID;

/**
 * @program: SecondKillMall
 * @description: UUID工具类
 * @author: liyuhua
 * @create:
 **/
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
