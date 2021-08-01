package com.liyuhua.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liyuhua.seckill.pojo.Goods;
import com.liyuhua.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-30
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @return
     * @param goodsId
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
