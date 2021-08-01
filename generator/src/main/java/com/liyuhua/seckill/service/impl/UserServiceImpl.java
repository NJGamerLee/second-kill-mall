package com.liyuhua.seckill.service.impl;

import com.liyuhua.seckill.pojo.User;
import com.liyuhua.seckill.mapper.UserMapper;
import com.liyuhua.seckill.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
