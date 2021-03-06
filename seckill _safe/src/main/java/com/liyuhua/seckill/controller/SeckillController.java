package com.liyuhua.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liyuhua.seckill.config.AccessLimit;
import com.liyuhua.seckill.exception.GlobalException;
import com.liyuhua.seckill.pojo.Order;
import com.liyuhua.seckill.pojo.SeckillMessage;
import com.liyuhua.seckill.pojo.SeckillOrder;
import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.rabbitmq.MQSender;
import com.liyuhua.seckill.service.IGoodsService;
import com.liyuhua.seckill.service.IOrderService;
import com.liyuhua.seckill.service.ISeckillOrderService;
import com.liyuhua.seckill.utils.JsonUtil;
import com.liyuhua.seckill.vo.GoodsVo;
import com.liyuhua.seckill.vo.RespBean;
import com.liyuhua.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController  implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

//    @RequestMapping("/doSeckill")
//    public String doSeckill(Model model, User user, Long goodsId){
//        if(user==null){
//            return"login";
//        }
//        model.addAttribute("user",user);
//        GoodsVo goods=goodsService.findGoodsVoByGoodsId(goodsId);
//        //????????????
//        if(goods.getStockCount()<1){
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        //????????????
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id",user.getId()).eq("goods_id",goodsId));
//        if(seckillOrder!=null){
//            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
//            return  "secKillFail";
//        }
//        Order order=orderService.seckill(user,goods);
//        model.addAttribute("order",order);
//        model.addAttribute("goods",goods);
//        return "orderDetail";
//    }

    //??????????????????
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     *
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId){
        System.out.println((user));
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user,goodsId,path);
        if (!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //????????????????????????
        String seckillOrderJson = (String) valueOperations
                .get("order:" + user.getId() + ":" + goodsId);
        if (!StringUtils.isEmpty(seckillOrderJson)) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        //????????????,??????Redis??????
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //????????????
        //Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate
                .execute(script, Collections.singletonList("seckillGoods:" + goodsId),
                        Collections.EMPTY_LIST);
        if (stock < 0) {
            EmptyStockMap.put(goodsId,true);//??????????????????redis??????
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // ????????????????????????????????????
        SeckillMessage message = new SeckillMessage(user, goodsId);
        mqSender.sendsecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

    /**
     *???????????????????????????
     * @param user
     * @param goodsId
     * @return orderId:?????????-1??????????????????0????????????
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * ?????????
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (null==user||goodsId<0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // ????????????????????????????????????
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //?????????????????????????????????redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId,
                captcha.text(),300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("?????????????????????",e.getMessage());
        }
    }


    //??????????????????????????????redis
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }

//        GoodsVo goods=goodsService.findGoodsVoByGoodsId(goodsId);
//        //????????????
//        if(goods.getStockCount()<1){
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
////        //????????????????????????
////        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
////                .eq("user_id",user.getId()).eq("goods_id",goodsId));
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue()
//                .get("order:"+user.getId()+":"+goodsId);
//        if(seckillOrder!=null){
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//        Order order=orderService.seckill(user,goods);
//        return RespBean.success(order);

}

