package com.mmall.service.imlp;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.service.ICategoryService;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import com.mmall.pojo.Cart;

import java.math.BigDecimal;
import java.util.List;

public class CartServiceImpl implements ICategoryService {

    @Autowired
    private CartMapper cartMapper;
    public ServerResponse add(Integer userId, Integer productId, int count){
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null){
            // 产品不在购物车中，需要新增记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else{
            // 产品已经在购物车中，数量加上count即可
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }

        return null;
    }


    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

    }
}{

        }
