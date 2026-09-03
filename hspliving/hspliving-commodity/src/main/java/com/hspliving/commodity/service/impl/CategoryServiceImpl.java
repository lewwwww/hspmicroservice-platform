package com.hspliving.commodity.service.impl;

import com.hspliving.commodity.vo.Catalog2Vo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.common.utils.Query;

import com.hspliving.commodity.dao.CategoryDao;
import com.hspliving.commodity.entity.CategoryEntity;
import com.hspliving.commodity.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    /**
     * 返回所有分类及其子分类（树形结构）
     * 实现：一次查库 + 按 parentId 分组（Map），递归组树时 O(1) 取子节点
     * 复杂度：由逐层全表扫描的 O(n²) 优化为 O(n)
     */
    @Override
    public List<CategoryEntity> listTree() {
        //1. 一次查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2. 按 parentId 分组，构建 父id -> 子分类列表 的映射（O(n)）
        Map<Long, List<CategoryEntity>> childrenMap = entities.stream()
                .collect(Collectors.groupingBy(CategoryEntity::getParentId));

        //3. 从根（parentId=0）开始递归组树，每层 O(1) 取子节点
        return buildTree(0L, childrenMap);
    }

    /**
     * 递归构建某一父节点下的子树
     */
    private List<CategoryEntity> buildTree(Long parentId, Map<Long, List<CategoryEntity>> childrenMap) {
        return childrenMap.getOrDefault(parentId, Collections.emptyList()).stream()
                .map(category -> {
                    category.setChildrenCategories(buildTree(category.getId(), childrenMap));
                    return category;
                })
                .sorted(Comparator.comparingInt(c -> c.getSort() == null ? 0 : c.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 返回 categoryId 的完整层级路径，如 [1,21,301]
     * 实现：一次查库构建 id->entity 映射，内存回溯父链
     * 优化：由每层递归查库（N+1 查询）改为 1 次查询 + 内存回查
     */
    @Override
    public Long[] getCascadedCategoryId(Long categoryId) {

        //1. 一次查出所有分类，构建 id -> 分类 映射
        Map<Long, CategoryEntity> entityMap = baseMapper.selectList(null).stream()
                .collect(Collectors.toMap(CategoryEntity::getId, Function.identity()));

        //2. 从当前分类沿 parentId 向上回溯，收集 [当前,父,根]
        List<Long> cascadedCategoryId = new ArrayList<>();
        Long currentId = categoryId;
        while (currentId != null && entityMap.containsKey(currentId)) {
            cascadedCategoryId.add(currentId);
            currentId = entityMap.get(currentId).getParentId();
        }

        //3. 翻转成 [根,父,当前]
        Collections.reverse(cascadedCategoryId);
        return cascadedCategoryId.toArray(new Long[cascadedCategoryId.size()]);
    }

    //返回当前所有的一级分类
    @Override
    public List<CategoryEntity> getLevel1Categories() {

        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", 0);
        List<CategoryEntity> categoryEntities =
                this.baseMapper.selectList(queryWrapper);

        return categoryEntities;
    }

    /**
     * 返回二级分类(包含三级分类)的数据-按照规定的格式Map<String, List<Catalog2Vo>>
     * 实现：一次查库 + 按 parentId 分组，各级分类均 O(1) 从映射取
     * 复杂度：由逐级全表扫描的 O(n²) 优化为 O(n)
     */
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        //- 一次查出所有分类，按 parentId 分组
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
        Map<Long, List<CategoryEntity>> childrenMap = selectList.stream()
                .collect(Collectors.groupingBy(CategoryEntity::getParentId));

        //- 得到所有的一级分类
        List<CategoryEntity> level1Categories =
                childrenMap.getOrDefault(0L, Collections.emptyList());

        Map<String, List<Catalog2Vo>> categoryMap =
                level1Categories.stream().collect(
                        Collectors.toMap(k -> k.getId().toString(),
                                v -> {
                                    List<Catalog2Vo> catalog2Vos = new ArrayList<>();

                                    //-得到当前一级分类对应的所有二级分类
                                    List<CategoryEntity> level2Categories =
                                            childrenMap.getOrDefault(v.getId(), Collections.emptyList());

                                    if (!level2Categories.isEmpty()) {
                                        catalog2Vos = level2Categories.stream().map(l2 -> {

                                            Catalog2Vo catalog2Vo =
                                                    new Catalog2Vo(v.getId().toString(), null, l2.getId().toString(), l2.getName());

                                            List<CategoryEntity> level3Categories =
                                                    childrenMap.getOrDefault(l2.getId(), Collections.emptyList());
                                            if (!level3Categories.isEmpty()) {
                                                List<Catalog2Vo.Category3Vo> category3Vos = level3Categories.stream().map(l3 ->
                                                        new Catalog2Vo.Category3Vo(l2.getId().toString(), l3.getId().toString(), l3.getName())
                                                ).collect(Collectors.toList());
                                                catalog2Vo.setCatalog3List(category3Vos);
                                            }
                                            return catalog2Vo;
                                        }).collect(Collectors.toList());
                                    }

                                    return catalog2Vos;
                                }));

        return categoryMap;
    }

}
