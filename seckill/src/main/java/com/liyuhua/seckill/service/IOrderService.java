package com.liyuhua.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liyuhua.seckill.pojo.Order;
import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.vo.GoodsVo;
import com.liyuhua.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-30
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goods);

    OrderDetailVo detail(Long orderId);

    //boolean checkPath(User user, Long goodsId, String path);

    //String createPath(User user, Long goodsId);
}
