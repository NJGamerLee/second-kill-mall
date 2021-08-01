package com.liyuhua.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.vo.LoginVo;
import com.liyuhua.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liyuhua
 * @since 2021-06-22
 */
public interface IUserService extends IService<User> {

    RespBean login(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie获取用户
     * @param userTicket
     * @return
     */
    User getUserByCookie(String userTicket,HttpServletRequest request,HttpServletResponse response);

    //RespBean updatePassword(String userTicket,Long id,String password);
    RespBean updatePassword(String userTicket ,String password,HttpServletRequest request, HttpServletResponse response);
}
