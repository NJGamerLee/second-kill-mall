package com.liyuhua.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyuhua.seckill.exception.GlobalException;
import com.liyuhua.seckill.mapper.UserMapper;
import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.service.IUserService;
import com.liyuhua.seckill.utils.CookieUtil;
import com.liyuhua.seckill.utils.MD5Util;
import com.liyuhua.seckill.utils.UUIDUtil;
import com.liyuhua.seckill.vo.LoginVo;
import com.liyuhua.seckill.vo.RespBean;
import com.liyuhua.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RespBean login(LoginVo loginvo, HttpServletRequest request, HttpServletResponse response){
        String mobile=loginvo.getMobile();
        String password=loginvo.getPassword();
//        //参数校验
//        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if(null==user){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //判断密码是否正确
        if(!MD5Util.formPassToDBPass(password,user.getSlat()).equals(user.getPassword())){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //生成cookie
        String ticket = UUIDUtil.uuid();
        //将用户信息存入redis
        redisTemplate.opsForValue().set("user:"+ticket,user);
        //request.getSession().setAttribute(ticket,user);
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return  RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket,HttpServletRequest request,HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:"+userTicket);
        if(user!=null){
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }
        return user;
    }

    @Override
//    public RespBean updatePassword(String userTicket, Long id, String password) {
//        User user = userMapper.selectById(id);
//        if (user == null) {
//            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
//        }
//        user.setPassword(MD5Util.inputPassToDbPass(password, user.getSalt()));
//        int result = userMapper.updateById(user);
//        if (1 == result) {
//            //删除Redis
//            redisTemplate.delete("user:" + userTicket);
//            return RespBean.success();
//        }
//        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
//    }
    public RespBean updatePassword(String userTicket,String password,HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket, request, response);
        if (user==null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDbPass(password,user.getSlat()));
        int result = userMapper.updateById(user);
        if (result==1) {
            //删除redis
            redisTemplate.delete("user:"+userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }

}
