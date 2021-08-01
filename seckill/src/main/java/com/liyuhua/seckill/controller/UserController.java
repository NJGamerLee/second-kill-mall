package com.liyuhua.seckill.controller;


import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.rabbitmq.MQSender;
import com.liyuhua.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-22
 */
@Controller
@RequestMapping("/user")
public class UserController {


    @Autowired
    private MQSender mqSender;

    /**
     * 用户信息
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }

//    //测试发送消息
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq() {
//        mqSender.send("Hello");
//    }
//
//    /**
//     * 测试发送RabbitMQ消息
//     */
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void mq01() {
//        mqSender.send("Hello");
//    }
//    /**
//     * 测试发送RabbitMQ消息
//     */
//    @RequestMapping("/mq/direct01")
//    @ResponseBody
//    public void mq02() {
//        mqSender.send01("Hello,Red");
//    }
//    /**
//     * 测试发送RabbitMQ消息
//     */
//    @RequestMapping("/mq/direct02")
//    @ResponseBody
//    public void mq03() {
//        mqSender.send02("Hello,Green");
//    }
//    /**
//     * 测试发送RabbitMQ消息
//     */
//    @RequestMapping("/mq/topic01")
//    @ResponseBody
//    public void mq04() {
//        mqSender.send01("Hello,Red");
//    }
//    /**
//     * 测试发送RabbitMQ消息
//     */
//    @RequestMapping("/mq/topic02")
//    @ResponseBody
//    public void mq05() {
//        mqSender.send02("Hello,Green");
//    }
}
