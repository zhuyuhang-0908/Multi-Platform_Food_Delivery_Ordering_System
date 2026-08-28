package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "订单管理接口")
@Slf4j

public class OrderController {
    @Autowired
    private OrderService orderService;


    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
         PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
         return Result.success(pageResult);
    }


//    /admin/order/statistics

//    /**
//     * 各个状态的订单数量统计
//     * @param orderStatisticsVO
//     * @return
//     */
//    @GetMapping("/statistics")
//    @ApiOperation("各个状态的订单数量统计")
//    public Result statistics (OrderStatisticsVO orderStatisticsVO){
//        //进行操作后，需要有接收的值，这里没有设置返回值，后续返回的是入参，
//        orderService.liststatistics(orderStatisticsVO);
//        return Result.success(orderStatisticsVO);
//    }


    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
///admin/order/details/{id}

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id){
        //要查order表  要查orderDetail表
        //要用到orderMapper orderDetailMapper
        //这里和用户端的查询订单详情共用一个details方法
        OrderVO orderVO = orderService.details(id);

        return Result.success(orderVO);
    }


///admin/order/confirm

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){

        //将订单的状态修改为“已结单”
        //前端点击接单，是要选中其中一个订单，点击其接单按钮，点击接单后返回的是订单信息，也就是跳转到订单详情页面
        //然后点击接单按钮，订单状态变为已结单
        orderService.confirm(ordersConfirmDTO);

        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception{

        orderService.rejection(ordersRejectionDTO);
        return Result.success();


    }


//    /admin/order

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception{
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }


    /**
     * 订单派送
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("订单派送")
    public Result delivery(@PathVariable("id") Long id){
        orderService.delivery(id);
        return Result.success();

    }

///admin/order
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id){
        orderService.complete(id);
        return Result.success();

    }





















}
