<template>
  <!-- 老师解读
   1. el-row 是elementui 的布局控件 参考 elementui官网 - Layout布局 -分栏间隔
   2. 我们将其分成两栏， 一栏放置分类树形菜单 一栏放置家居分类属性组的表单-->
  <el-row :gutter="20">
    <el-col :span="5">
      <!--
        1. 分类树形占5个单元宽度, 保留ref, :data, :props, node-key 基本显示的属性
        2. node-click实际是el-tree中规定好的实际，会传递三个参数: 参考elmentui官网 elementui-Tree 树形控件-Events -->
      <el-tree ref="categoryTree" :data="data" :props="defaultProps"
               node-key="id" @node-click="nodeclick">
        <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
      </span>
      </el-tree>
    </el-col>
    <el-col :span="19">

      <!-- 说明 原家居分类属性组的表单放在这里 带div.  -->
      <div class="mod-config">
        <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
          <el-form-item>
            <el-input v-model="dataForm.key" placeholder="参数名" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button @click="getDataList()">查询</el-button>
            <el-button v-if="isAuth('commodity:attrgroup:save')" type="primary" @click="addOrUpdateHandle()">新增
            </el-button>
            <el-button v-if="isAuth('commodity:attrgroup:delete')" type="danger" @click="deleteHandle()"
                       :disabled="dataListSelections.length <= 0">批量删除
            </el-button>
          </el-form-item>
        </el-form>
        <el-table
          :data="dataList"
          border
          v-loading="dataListLoading"
          @selection-change="selectionChangeHandle"
          style="width: 100%;">
          <el-table-column
            type="selection"
            header-align="center"
            align="center"
            width="50">
          </el-table-column>
          <el-table-column
            prop="id"
            header-align="center"
            align="center"
            label="id">
          </el-table-column>
          <el-table-column
            prop="name"
            header-align="center"
            align="center"
            label="组名">
          </el-table-column>
          <el-table-column
            prop="sort"
            header-align="center"
            align="center"
            label="排序">
          </el-table-column>
          <el-table-column
            prop="description"
            header-align="center"
            align="center"
            label="说明">
          </el-table-column>
          <el-table-column
            prop="icon"
            header-align="center"
            align="center"
            label="组图标">
          </el-table-column>
          <el-table-column
            prop="categoryId"
            header-align="center"
            align="center"
            label="所属分类id">
          </el-table-column>
          <el-table-column
            fixed="right"
            header-align="center"
            align="center"
            width="150"
            label="操作">
            <template slot-scope="scope">
              <el-button type="text" size="small"
                         @click="relationAttrHandle(scope.row.id)">关联
              </el-button>
              <el-button type="text" size="small" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
              <el-button type="text" size="small" @click="deleteHandle(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          @size-change="sizeChangeHandle"
          @current-change="currentChangeHandle"
          :current-page="pageIndex"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :total="totalPage"
          layout="total, sizes, prev, pager, next, jumper">
        </el-pagination>
        <!-- 弹窗, 新增 / 修改 -->
        <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
        <!-- 维护关联关系的组件 添加到add-or-update下面 -->
        <RelationUpdate v-if="relationVisible" ref="relationUpdate" @refreshData="getDataList"></RelationUpdate>
      </div>
    </el-col>
  </el-row>

</template>

<script>
import AddOrUpdate from './attrgroup-add-or-update'
//引入组件
import RelationUpdate from "./attrgroup-attr-relation";

export default {
  data() {
    return {
      // 显示某个属性分组下关联的所有商品属性(基本属性)情况
      relationVisible: false,
      dataForm: {
        key: ''
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      addOrUpdateVisible: false,
      data: [],
      defaultProps: { //分类树形默认属性, 需要和返回的数据格式和属性名对应
        children: 'childrenCategories',
        label: 'name'
      },
      catId: 0 // catId 默认为0, 表示在查询属性分组信息时，查询条件不带 category_id
    }
  },
  components: { //这个是我们讲解vue技术时讲过知识点
    //引入 RelationUpdate
    AddOrUpdate, RelationUpdate
  },
  activated() {
    this.getDataList()
  },
  created() { //这个方法是生命周期方法，前面讲过而是使用过多次
    this.getCategories();
  },
  methods: {
    //添加方法relationAttrHandle
    //处理分组与属性的关联
    relationAttrHandle(groupId) {
      this.relationVisible = true;
      // console.log("groupId-", groupId)
      this.$nextTick(() => {
        this.$refs.relationUpdate.init(groupId);
      });
    },
    // 响应用户点击树形控件的第三级菜单
    nodeclick(data) {
      // console.log("data=>", data)
      if (data.catLevel == 3) {//点击的第三级分类
        this.catId = data.id
        this.getDataList()
      }
    },
    // 获取分类列表,带层级, 老师这里就直接拿来使用，没有做抽取-前端知道怎么使用即可
    getCategories() {
      this.$http({
        // 请求 url
        // url: 'http://localhost:9090/commodity/category/list/tree',
        // url 就是 http://localhost:5050/api/commodity/category/list/tree
        url: this.$http.adornUrl('/commodity/category/list/tree'),
        method: 'get'
      }).then(({data}) => { //{data} 是解构了返回数据的data部分
        //输出
        console.log("返回的data= ", data)
        //返回需要展示的数据 data.data.data
        //如果使用了{data}, 得到要展示的数据 data.data
        this.data = data.data
      })
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true
      this.$http({
        // 分析, 为什么就获取到所以的属性分组信息
        // 完整url 是 http://localhost:5050/api/commodity/attrgroup/list
        url: this.$http.adornUrl(`/commodity/attrgroup/list/${this.catId}`),
        method: 'get',
        params: this.$http.adornParams({
          'page': this.pageIndex,
          'limit': this.pageSize,
          'key': this.dataForm.key
        })
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.dataList = data.page.list
          this.totalPage = data.page.totalCount
        } else {
          this.dataList = []
          this.totalPage = 0
        }
        this.dataListLoading = false
      })
    },
    // 每页数
    sizeChangeHandle(val) {
      this.pageSize = val
      this.pageIndex = 1
      this.getDataList()
    },
    // 当前页
    currentChangeHandle(val) {
      this.pageIndex = val
      this.getDataList()
    },
    // 多选
    selectionChangeHandle(val) {
      this.dataListSelections = val
    },
    // 新增 / 修改
    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id)
      })
    },
    // 删除
    deleteHandle(id) {
      var ids = id ? [id] : this.dataListSelections.map(item => {
        return item.id
      })
      this.$confirm(`确定对[id=${ids.join(',')}]进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/commodity/attrgroup/delete'),
          method: 'post',
          data: this.$http.adornData(ids, false)
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.$message({
              message: '操作成功',
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.getDataList()
              }
            })
          } else {
            this.$message.error(data.msg)
          }
        })
      })
    }
  }
}
</script>
