<template>
  <!-- 处理品牌的查询和删除 -->
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.key" placeholder="参数名" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">查询</el-button>
        <el-button v-if="isAuth('commodity:brand:save')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
        <el-button v-if="isAuth('commodity:brand:delete')" type="danger" @click="deleteHandle()"
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
        label="品牌名">
      </el-table-column>
      <el-table-column
        prop="logo"
        header-align="center"
        align="center"
        label="logo">
        <!-- 使用插槽机制, 给表格的列插入 template, 显示logo图片 -->
        <template slot-scope="scope">
          <img :src="scope.row.logo" style="width: 80px">
        </template>
      </el-table-column>
      <el-table-column
        prop="description"
        header-align="center"
        align="center"
        label="说明">
      </el-table-column>
      <el-table-column
        prop="isshow"
        header-align="center"
        align="center"
        label="显示">
        <!--
         老韩解读
         1. 使用插槽机制，给表格的列加入一个template , 这可以参考ElementUI-Table 表格-自定义列模板的代码
         2. 在template中替换成el-switch控件，参考ElementUI-Switch 开关-基本用法
         3. v-model="scope.row.isshow" 动态绑定当前行的isshow的值
         4. active-color / inactive-color 是激活和未激活的颜色
         5. active-value="1" 表示激活状态的值和表的字段isShow对应
         6. inactive-value="0" 表示未激活的状态的值，和表的字段 isShow
         7. @change="changeIsShow(scope.row)" 当switch控件状态切换时，会触发 changeIsShow
            ,而且我们把 scope.row 传递到该方法
        -->
        <template slot-scope="scope">
          <!-- 引入el-switch -->
          <el-switch
            v-model="scope.row.isshow"
            @change="changeIsShow(scope.row)"
            :active-value="1"
            :inactive-value="0"
            active-color="#13ce66"
            inactive-color="#ff4949">
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column
        prop="firstLetter"
        header-align="center"
        align="center"
        label="检索首字母">
      </el-table-column>
      <el-table-column
        prop="sort"
        header-align="center"
        align="center"
        label="排序">
      </el-table-column>
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="150"
        label="操作">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="relateCategoryHandle(scope.row.id)">关联分类</el-button>
          <el-button type="text" size="small" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
          <el-button type="text" size="small" @click="deleteHandle(scope.row.id, scope.row.name)">删除</el-button>
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

    <!-- 品牌和分类关联的对话框 -->
    <el-dialog title="关联分类" :visible.sync="cateRelationDialogVisible" width="30%">
      <el-popover placement="right-end" v-model="popCatelogSelectVisible">
        <!--        <category-cascader :catelogPath.sync="catelogPath"></category-cascader>-->
        <!-- 这里我们加入分类的Cascader 级联选择器, 前面我们使用过 -->
        <el-cascader v-model="cascadedCategoryId" :options="categories" :props="props"></el-cascader>
        <div style="text-align: right; margin: 0">
          <el-button size="mini" type="text" @click="popCatelogSelectVisible = false">取消</el-button>
          <el-button type="primary" size="mini" @click="addBrandCategoryRelation">确定</el-button>
        </div>
        <el-button slot="reference">新增关联</el-button>
      </el-popover>
      <el-table :data="cateRelationTableData" style="width: 100%">
        <el-table-column prop="id" label="#"></el-table-column>
        <el-table-column prop="brandName" label="品牌名"></el-table-column>
        <el-table-column prop="categoryName" label="分类名"></el-table-column>
        <el-table-column fixed="right" header-align="center" align="center" label="操作">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="deleteCateRelationHandle(scope.row.id,scope.row.brandId)"
            >移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
    <el-button @click="cateRelationDialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="cateRelationDialogVisible = false">确 定</el-button>
  </span>
    </el-dialog>

  </div>
</template>

<script>
import AddOrUpdate from './brand-add-or-update'

export default {
  data() {
    return {
      // 针对点击关联分类的一些数据
      cateRelationDialogVisible: false, // 控制对话框是否显示，默认不显示
      cateRelationTableData: [], // 绑定品牌和分类的表格数据
      cascadedCategoryId: [], // 和 Cascader 级联选择器 显示分类层级数据绑定 如:[1,21,301]
      popCatelogSelectVisible: false, // 控制el-popover控件是否显示
      props: {       //显示返回的家居分类的哪些字段/信息, 前面说过和 Cascader 级联选择器关联的
        value: "id",
        label: "name",
        children: "childrenCategories"
      },
      categories: [], //所有的家居分类
      brandId: 0, // 记录你选择的品牌id
      dataForm: {
        key: ''
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      addOrUpdateVisible: false
    }
  },
  components: {
    AddOrUpdate
  },
  activated() {
    this.getDataList()
  },
  created() {
    this.getCategories()
  },
  methods: {
    // 增加方法，响应删除 品牌和分类关联的处理
    deleteCateRelationHandle(id) {
      // console.log("id->", id)
      //1. 获取删除的分类的id
      var ids = [id]
      this.$confirm(`是否删除该记录?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        //2. 如果点击确认 , 发出请求, 完成删除
        this.$http({
          // 请求 url, 走网关
          url: this.$http.adornUrl('/commodity/categorybrandrelation/delete'),
          method: 'post',
          //发出请求时，携带的数据
          data: this.$http.adornData(ids, false)
        }).then(({data}) => { //{data} 是解构了返回数据的data部分
          this.$message({
            message: '删除操作成功',
            type: 'success'
          })
          //刷新 品牌和分类关联列表
          this.getCategoryBrandRelation()
        })
      }).catch(() => { //点击取消，就不删除了
      })
    },
    // 增加方法, 请求后端对应的API, 根据brandId返回 品牌和分类对应的记录
    getCategoryBrandRelation() {

      this.$http({
        url: this.$http.adornUrl('/commodity/categorybrandrelation/brand/list'),
        method: 'get',
        // 携带的参数
        params: this.$http.adornParams({
          'brandId': this.brandId
        })
      }).then(({data}) => {
        // 测试看看是否得到的返回的数据
        // console.log("data->", data)
        this.cateRelationTableData = data.data
      })

    },

    // 增加将关联关系【品牌-分类】, 保存到数据表
    addBrandCategoryRelation() {

      // 当添加结束后，需要不显示 el-popover
      this.popCatelogSelectVisible = false
      // 发送请求，添加品牌-分类 关联关系
      this.$http({
        url: this.$http.adornUrl('/commodity/categorybrandrelation/save'),
        method: 'post',
        //发出请求时，携带的数据
        data: this.$http.adornData({
          brandId: this.brandId,
          categoryId: this.cascadedCategoryId[this.cascadedCategoryId.length - 1]
        }, false)
      }).then(({data}) => {
        // 添加成功后，可以刷新显示 品牌和分类的列表
        this.getCategoryBrandRelation()
      })
    },
    // 获取分类列表,带层级
    getCategories() {
      this.$http({
        url: this.$http.adornUrl('/commodity/category/list/tree'),
        method: 'get'
      }).then(({data}) => { //{data} 是解构了返回数据的data部分
        this.categories = data.data
      })
    },
    // 响应用户操作关联分类按钮
    relateCategoryHandle(id) {
      // console.log("id->", id)
      this.cateRelationDialogVisible = true
      // 将当前选择的品牌id 赋给 this.brandId
      this.brandId = id

      //调用方法，返回当前brandId对应的 品牌和分类记录
      this.getCategoryBrandRelation()
    },
    // 响应用户切换switch的操作
    changeIsShow(data) {
      // console.log(data.id, " ", data.isshow)
      // data 就是 row
      // 因为我们是通过id ,修改isShow , 所以我们只需要发送时，携带 id 和 isShow
      let {id, isshow} = data
      // 发送请求，修改对应品牌记录的isShow字段值
      this.$http({
        // 请求 url
        // url: 'http://localhost:9090/commodity/brand/update',
        url: this.$http.adornUrl('/commodity/brand/update/isshow'),
        method: 'post',
        //发出请求时，携带的数据
        //多说一下: {id, isshow} 是json数据, 完整写法是{"id":id,"isshow": isshow}
        //当key/属性名 和 值/值名一致时，可以简写成 {id,isshow}
        data: this.$http.adornData({id, isshow}, false)
      }).then(({data}) => {
        //输出
        this.$message({
          message: '修改品牌显示状态成功OK',
          type: 'success'
        })
      })
    },
    // 获取数据列表-查询
    getDataList() {
      this.dataListLoading = true
      this.$http({
        url: this.$http.adornUrl('/commodity/brand/list'),
        // url: 'http://localhost:9090/commodity/brand/list',
        method: 'get',
        // 携带的参数
        params: this.$http.adornParams({
          'page': this.pageIndex,
          'limit': this.pageSize,
          'name': this.dataForm.key
        })
      }).then(({data}) => {
        if (data && data.code === 0) {
          this.dataList = data.page.list
          this.totalPage = data.page.totalCount
          console.log("this.dataList=>", this.dataList)
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
    // 删除 [批量删除 - 指定删除]
    deleteHandle(id, name) {
      // 收集到要删除的id
      var ids = id ? [id] : this.dataListSelections.map(item => {
        return item.id
      })
      //模仿ids的获取方式，来获取要删除的brand的name, 收集到names
      var names = id ? [name] : this.dataListSelections.map(item => {
        return item.name
      })
      // this.$confirm(`确定对[id=${ids.join(',')}]进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
      this.$confirm(`确定对[name=${names.join(',')}]进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          // url: this.$http.adornUrl('/commodity/brand/delete'),
          url: 'http://localhost:9090/commodity/brand/delete',
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
