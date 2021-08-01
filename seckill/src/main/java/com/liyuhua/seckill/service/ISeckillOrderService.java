package com.liyuhua.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liyuhua.seckill.pojo.SeckillOrder;
import com.liyuhua.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-30
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
