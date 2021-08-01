package com.liyuhua.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyuhua.seckill.exception.GlobalException;
import com.liyuhua.seckill.mapper.OrderMapper;
import com.liyuhua.seckill.pojo.Order;
import com.liyuhua.seckill.pojo.SeckillGoods;
import com.liyuhua.seckill.pojo.SeckillOrder;
import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.service.IGoodsService;
import com.liyuhua.seckill.service.IOrderService;
import com.liyuhua.seckill.service.ISeckillGoodsService;
import com.liyuhua.seckill.service.ISeckillOrderService;
import com.liyuhua.seckill.utils.JsonUtil;
import com.liyuhua.seckill.utils.MD5Util;
import com.liyuhua.seckill.utils.UUIDUtil;
import com.liyuhua.seckill.vo.GoodsVo;
import com.liyuhua.seckill.vo.OrderDetailVo;
import com.liyuhua.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public Order seckill(User user, GoodsVo goods){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id",goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        boolean seckillGoodsResult =  seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count - 1")
                .eq("goods_id", goods.getId())
                .gt("stock_count", 0));
        // seckillGoodsService.updateById(seckillGoods);
        if (!seckillGoodsResult) {
            //判断是否还有库存
            valueOperations.set("isStockEmpty:" + goods.getId(), "0");
            return null;
        }

        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliverAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);

        valueOperations.set("order:" + user.getId() + ":" + goods.getId(),
                JsonUtil.object2JsonStr(seckillOrder));
        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if (null==orderId){
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo();
        detail.setGoodsVo(goodsVo);
        detail.setOrder(order);
        return detail;
    }

    /**
     * 验证请求地址
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
//    @Override
//    public boolean checkPath(User user, Long goodsId, String path) {
//        if (user==null|| StringUtils.isEmpty(path)){
//            return false;
//        }
//        String redisPath = (String) redisTemplate.opsForValue()
//                .get("seckillPath:" + user.getId() + ":" + goodsId);
//        return path.equals(redisPath);
//    }
//
//    /**
//     * 生成秒杀地址
//     *
//     * @param user
//     * @param goodsId
//     * @return
//     */
//    @Override
//    public String createPath(User user, Long goodsId) {
//        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
//        redisTemplate.opsForValue()
//                .set("seckillPath:" + user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);//随机化接口地址，1分钟失效
//        return str;
//    }

}
