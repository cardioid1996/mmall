package com.mmall.service.imlp;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
//import com.sun.xml.internal.ws.server.ServerRtException;
//import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在！");
        }
        System.out.println("pwd: " + password);
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        System.out.println("after md5:" + md5Password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null){
            return ServerResponse.createByErrorMessage("密码错误！");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功！", user);
    }


    @Override
    public ServerResponse<String> register(User user){

        ServerResponse<String> usernameValidation = checkValid(user.getUsername(), Const.USERNAME);
        if (!usernameValidation.isSuccess())
            return usernameValidation;

        ServerResponse<String> emailValidation = checkValid(user.getEmail(), Const.EMAIL);
        if (!emailValidation.isSuccess())
            return emailValidation;

        user.setRole(Const.Role.ROLE_CUSTOMER);
        // md5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");

    }

    @Override
    public ServerResponse<String> checkValid(String str, String type){
        if (StringUtils.isNotBlank(type)){
            //开始校验
            if (Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0){
                    return ServerResponse.createByErrorMessage("邮箱已存在！");
                }
            }
        }
        else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");

    }

    public ServerResponse<String> selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题未设置");
    }


    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0){
            //问题及回答都属于该用户，且正确
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        else
            return ServerResponse.createByErrorMessage("找回密码错误");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if (StringUtils.isBlank(forgetToken))
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        if(StringUtils.equals(forgetToken, token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);

            if (rowCount > 0)
                return ServerResponse.createBySuccessMessage("修改密码成功");
        }
        else{
            // message 是显示在前端的
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        String md5OldPassword = MD5Util.MD5EncodeUtf8(passwordOld);
        int resultCount = userMapper.checkPassword(md5OldPassword, user.getId());
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        // 选择性更新
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0)
            return ServerResponse.createBySuccessMessage("密码更新成功");
        return ServerResponse.createByErrorMessage("密码更新失败");
    }


    public ServerResponse<User> updateInfomation(User user){
        // username不能被更新
        // email校验是否在已存在于其他人的信息里
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0){
            return ServerResponse.createByErrorMessage("该邮箱已被占用");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新对象成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新对象发生错误");
    }


    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


    public ServerResponse checkAdminRole(User user){
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }



}
