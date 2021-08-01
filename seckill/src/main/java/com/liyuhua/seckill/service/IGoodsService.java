package com.liyuhua.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liyuhua.seckill.pojo.Goods;
import com.liyuhua.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-30
 */
public interface IGoodsService extends IService<Goods> {
    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
