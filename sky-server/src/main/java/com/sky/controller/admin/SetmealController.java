package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    //新增之后，用户从缓存中拿到分类下面的套餐与新增之前的不一致，所以要删除该分类下所有套餐，用户再查一遍数据就一致了，
    // 因此这里是删除的某个具体的套餐分类id下的所有套餐数据。数据精确查询
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames ="setmealCache",key = "#setmealDTO.categoryId")  //key: setmealCache::100  删除缓存中的所有的套餐
    //在管理端有数据变动了必须清理缓存，不然用户读到的数据就是缓存里的脏数据。
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}",setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();

    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 套餐批量删除
     * @param ids     这里ids表示每个套餐的的id的集合，
     * @return
     */
    @DeleteMapping
    @ApiOperation("套餐批量删除")
    //这里由于是批量删除，可能只删除一个套餐分类下的套餐，也可能是删除了涉及到好几个套餐分类下的套餐，所以不好处理，故，直接删除。
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("套餐批量删除：{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }


    /**
     * 根据id查询套餐   用于页面回显
     * @param id      每个id代表具体每个套餐
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐");
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    //修改套餐 的话，可能会修改到套餐的分类，会涉及两个到两个套餐分类下的变动，
    // 比如A中少了1菜，1菜去了B中这种，还有就是只是单纯修改菜品价格或者口味的，综上过于复杂
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐：{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }


    /**
     * 套餐启售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐启售停售")

    //还是那个意思，停售或启售的套餐可能是不同套餐分类下的，所以直接全部清理缓存，重新更新。
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id){
        setmealService.startOrStop(status,id);
        return Result.success();
    }



















}
