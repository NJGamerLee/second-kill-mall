package com.liyuhua.seckill.service.impl;

import com.liyuhua.seckill.pojo.Order;
import com.liyuhua.seckill.mapper.OrderMapper;
import com.liyuhua.seckill.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-30
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}
