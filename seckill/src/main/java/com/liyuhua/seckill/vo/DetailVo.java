package com.liyuhua.seckill.vo;

import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.vo.GoodsVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {
    private User user;
    private GoodsVo goodsVo;
    private int secKillStatus;
    private int remainSeconds;
}