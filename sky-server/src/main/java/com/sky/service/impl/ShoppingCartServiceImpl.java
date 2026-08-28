package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    //如果是添加的相同商品，那么就设置后面的数目加一，若是添加的商品不存在，则在购物车里新增一条数据，调用insert
    //不用的用户有需要自己的购物车(user_id)
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO){
        //select * from shopping_cart where user_id=? and setmeal_id=xx  当用户加购的是套餐时
        //select * from shopping_cart where user_id=? and dish_id= xx and dish flavor 当用户加购的是菜品时
        //判断当前加入到购物车的商品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);  //dto里面没有登录用户的id(这里只拷贝了三个属性，其余的后续设置)
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);  ////设置shoppingCartDTO中没有的属性值，从dish中获取，设置给这里的shoppingCart


        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);


        //如果已经存在了，只需要将数量加1

        if(list != null && list.size() > 0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);//update shopping_cart set number = ? where id = ?
            shoppingCartMapper.updateNumberById(cart);
        }else {
            //如果不存在，需要插入一条购物车数据

            //判断本次添加到购物车的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();  //根据前端的dto，获取菜品的id
            if(dishId != null){
                //本次添加到购物车的是菜品
                Dish dish = dishMapper.getById(dishId);    //然后根据菜品id查菜品，新的菜品就是这个菜品。
                shoppingCart.setName(dish.getName());   //  设置shoppingCartDTO中没有的属性值，从dish中获取，设置给这里的shoppingCart
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {

                //本次添加到购物车的是套餐
                //先把套餐的id获取出来
                Long setmealId = shoppingCartDTO.getSetmealId();
                //不再判断setmealId是不是空了，因为dish为空，那么setmeal一定不为空

                //根据id把套餐查上来
                Setmeal setmeal = setmealMapper.getById(setmealId);

                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            shoppingCart.setNumber(1);   //设置shoppingCartDTO中没有的属性值，从dish中获取，设置给这里的shoppingCart
            shoppingCart.setCreateTime(LocalDateTime.now());  //设置shoppingCartDTO中没有的属性值，从dish中获取，设置给这里的shoppingCart
            shoppingCartMapper.insert(shoppingCart);   //调用mapper将处理好的数据插入

        }




    }

    /**
     * 查看购物车
     * @return
     */
    public List<ShoppingCart> showShoppingCart(){
        //获取到当前微信用户的id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    public void cleanShoppingCart(){
        //获取到当前微信用户的id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);

    }


    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO){
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //设置查询条件，查询当前登录用户的购物车数据
        //这里的说明其实有一些混淆，不应该说查询购物车数据，并不是指查出来该用户的所有购物车数据
        //而是，只将前端点击的那一次的菜品的数据给查出来，也就是说，下面的list的值只可能为1，
        // 要是为0的话，是不可能的，因为前端显示已经有这个菜品了，才有后续的减少操作
        shoppingCart.setUserId(BaseContext.getCurrentId());


        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list != null && list.size() > 0){
            shoppingCart = list.get(0);  //获取到该菜品1

            Integer number = shoppingCart.getNumber();
            if(number == 1){
                //当前商品在购物车中的份数为1，直接删除当前记录
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else{
                //当前商品在购物车中的份数不为1，直接删除当前记录
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }

        }


    }


}
