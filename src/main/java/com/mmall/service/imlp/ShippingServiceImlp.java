package com.mmall.service.imlp;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("iShippingService")
public class ShippingServiceImlp implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse<Map> add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        else{
            return ServerResponse.createByErrorMessage("新建地址出错");
        }
    }


    public ServerResponse<String> del(Integer userId, Integer shippingId){
        int rowCount = shippingMapper.deleteByShippingIdUserId(userId, shippingId);
        if (rowCount > 0){
            Map result = Maps.newHashMap();
            return ServerResponse.createBySuccess("删除地址成功");
        }
        else{
            return ServerResponse.createByErrorMessage("删除地址出错");
        }
    }


    public ServerResponse<String> update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShipping(shipping);
        if (rowCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        else{
            return ServerResponse.createByErrorMessage("更新地址出错");
        }
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
        if (shipping != null){
            return ServerResponse.createBySuccess("查询地址成功", shipping);
        }
        else{
            return ServerResponse.createByErrorMessage("查询地址出错");
        }
    }

    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }


}



