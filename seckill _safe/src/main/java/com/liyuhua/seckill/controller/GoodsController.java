package com.liyuhua.seckill.controller;

import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.service.IGoodsService;
import com.liyuhua.seckill.service.IUserService;
import com.liyuhua.seckill.vo.DetailVo;
import com.liyuhua.seckill.vo.GoodsVo;
import com.liyuhua.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    /**
     * 跳转到商品列表页面
     */
    @Autowired
    private IUserService userService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * tolist
     * @param model
     * @param user
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user,HttpServletRequest request, HttpServletResponse response){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //Redis中获取页面，如果不为空，直接返回页面
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());
        // return "goodsList";
        //如果为空，手动渲染，存入Redis并返回
        WebContext context = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);//失效时间
        }
        return html;
    }
    /**
     * 跳转商品详情
     */
//    @RequestMapping("/toDetail/{goodsId}")
//    public String toDetail(Model model, User user, @PathVariable Long goodsId) {
//        model.addAttribute("user", user);
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        model.addAttribute("goods", goods);
//        Date startDate = goods.getStartDate();
//        Date endDate = goods.getEndDate();
//        Date nowDate = new Date();
//        //秒杀状态
//        int secKillStatus = 0;
//        //剩余开始时间
//        int remainSeconds = 0;
//        //秒杀还未开始
//        if (nowDate.before(startDate)) {
//            remainSeconds = (int) ((startDate.getTime()-nowDate.getTime())/1000);
//            // 秒杀已结束
//        } else if (nowDate.after(endDate)) {
//            secKillStatus = 2;
//            remainSeconds = -1;
//            // 秒杀中
//        } else {
//            secKillStatus = 1;
//            remainSeconds = 0;
//        }
//        model.addAttribute("secKillStatus",secKillStatus);
//        model.addAttribute("remainSeconds",remainSeconds);
//        return "goodsDetail";
//    }




//    @RequestMapping(value = "/toDetail/{goodsId}",produces = "text/html;charset=utf-8")
//    @ResponseBody
//    public String toDetail(HttpServletRequest request, HttpServletResponse response,
//                           Model model, User user, @PathVariable Long goodsId){
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        //Redis中获取页面，如果不为空，直接返回页面
//        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
//        if (!StringUtils.isEmpty(html)) {
//            return html;
//        }
//        model.addAttribute("user",user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        model.addAttribute("goods",goodsVo);
//        Date startDate=goodsVo.getStartDate();
//        Date endDate=goodsVo.getEndDate();
//        Date nowDate=new Date();
//        //秒杀状态
//        int secKillStatus = 0;
//        int remainSeconds = 0;
//        //未开始
//        if(nowDate.before(startDate)){
//            remainSeconds = (int)((startDate.getTime()-nowDate.getTime()))/1000;
//        }else if (nowDate.after(endDate)){
//            //已结束
//            secKillStatus = 2;
//            remainSeconds = -1;
//        }else{
//            //进行中
//            secKillStatus = 1;
//            remainSeconds = 0;
//        }
//        model.addAttribute("remainSeconds",remainSeconds);
//        model.addAttribute("secKillStatus",secKillStatus);
//        // return "goodsDetail";
//        //如果为空，手动渲染，存入Redis并返回
//        WebContext context = new WebContext(request, response,
//                request.getServletContext(), request.getLocale(),model.asMap());
//        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
//        if (!StringUtils.isEmpty(html)) {
//            valueOperations.set("goodsDetail:" + goodsId, html, 60,
//                    TimeUnit.SECONDS);
//        }
//        return html;
//    }
//
//
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId){
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate=goodsVo.getStartDate();
        Date endDate=goodsVo.getEndDate();
        Date nowDate=new Date();

        //秒杀状态
        int secKillStatus = 0;
        int remainSeconds = 0;
        //未开始
        if(nowDate.before(startDate)){
            remainSeconds = (int)((startDate.getTime()-nowDate.getTime()))/1000;
        }else if (nowDate.after(endDate)){
            //已结束
            secKillStatus = 2;
            remainSeconds = -1;
        }else{
            //进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }

        DetailVo detailVo = new DetailVo();
        detailVo.setGoodsVo(goodsVo);
        detailVo.setUser(user);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSecKillStatus(secKillStatus);
        return RespBean.success(detailVo);
    }
}