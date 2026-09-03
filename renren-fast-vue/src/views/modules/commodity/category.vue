<template>
  <div>
    <!-- 引入批量删除的按钮 -->
    <el-button type="danger" @click="batchDelete">批量删除</el-button>
    <el-tree :data="data"
             :props="defaultProps"
             ref="categoryTree"
             show-checkbox
             :default-expanded-keys="expandedKey"
             node-key="id"
             :expand-on-click-node="false">
      <!--    老师解读 (增加的属性，方法等，都可以参考elementUI Tree 树形控件 的文档说明)
              1. 使用vue的插槽机制在每个菜单后加入 Append和Delete 链接
              2. slot-scope="{ node, data } : 是一种解构写法, node 表示当前节点, data 就是从数据库取出的分类对象信息
              3. @node-click="handleNodeClick" 没有什么用，删除，注意把对应的method也删除
              4. 增加相应的append(data) 和 remove(node, data) 方法
              5. :expand-on-click-node="false": 设置false表示点击Append和Delete 链接 不展开下级分类
              6. show-checkbox 分类项显示成复选框，在删除时，可以支持多选
              7. node-key="id" 使用分类信息的id来唯一标识一个node-->
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)">
            Append
          </el-button>
          <!-- 引入edit按钮 -->
          <el-button
            type="text"
            size="mini"
            @click="() => edit(data)">
            Edit
          </el-button>
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)">
            Delete
          </el-button>
        </span>
      </span>
    </el-tree>
    <!--引入了对话框
      老师解读
      1. :visible.sync="dialogVisible" 动态绑定dialogVisible
         如果 dialogVisible 为true ,就显示对话框,否则就不显示
      2. :model="category" 动态绑定category数据对象, category在数据池定义
    -->
    <el-dialog title="添加/修改 分类" :visible.sync="dialogVisible" width="30%">

      <el-form :model="category">
        <el-form-item label="分类名">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <!-- 增加 修改需要的字段, 这里也会影响到添加 -->
        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="category.proUnit" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="addOrUpdate">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      data: [],
      dialogType: "", //标识对话框的类型 "add" , "edit"
      dialogVisible: false, //控制是否显示对话框
      category: { //初始化添加分类的表单对象数据, 注意category的属性名和CategoryEntity属性/字段 对应
        id: null,
        name: "",
        parentId: 0,
        catLevel: 0,
        isShow: 1,
        sort: 0,
        icon: "",
        proUnit: "",
        proCount: 0
      }, //添加的分类数据
      expandedKey: [],
      defaultProps: { //默认属性, 需要和返回的数据格式和属性名对应
        children: 'childrenCategories',
        label: 'name'
      }
    };
  },
  methods: {
    //处理批量删除
    batchDelete() {

      //通过ref="categoryTree" 来获取勾选的分类信息
      /**
       * 老韩解读 this.$refs.categoryTree.getCheckedNodes(leafOnly, includeHalfChecked)
       * 1. this.$refs.categoryTree : 代表 el-tree 控件
       * 2. getCheckedNodes(leafOnly, includeHalfChecked)
       * 2.1 若节点可被选择（即 show-checkbox 为 true），则返回目前被选中的节点所组成的数组
       * 2.2 (leafOnly, includeHalfChecked) 接收两个 boolean 类型的参数，
       * (1). 是否只是叶子节点，默认值为 false (2). 是否包含半选节点，默认值为 false
       * 2.3 老师没有传入参数 , 等价getCheckedNodes(false, false), 即返回选中的分类节点(不包括半选的)
       * 2.4 半选就是某个分类的子分类, 只选择了部分，没有全选，是 - 符号
       */
      var checkedNodes = this.$refs.categoryTree.getCheckedNodes()
      //先收集 选中的分类的ids 和 分类名
      var ids = []
      var categoryNames = []
      for (var i = 0; i < checkedNodes.length; i++) {
        ids.push(checkedNodes[i].id)
        categoryNames.push(checkedNodes[i].name)
      }
      //可以给出提示，如果用户选择确定，就调用后端程序的接口，完成批量删除(逻辑删除)
      this.$confirm(`是否批量删除【${categoryNames}】菜单?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        //2. 如果点击确认 , 发出请求, 完成批量删除
        this.$http({
          // 请求 url
          url: 'http://localhost:9090/commodity/category/delete',
          method: 'post',
          //发出请求时，携带的数据
          data: this.$http.adornData(ids, false)
        }).then(({data}) => { //{data} 是解构了返回数据的data部分
          //输出
          //console.log("返回的data= ", data)
          this.$message({
            message: '批量删除成功OK',
            type: 'success'
          })
          //刷新分类列表
          this.getCategories()
        })
      }).catch(() => { //点击取消，就不删除了
      })

    },
    //增加方法addOrUpdate - 可以处理添加分类和修改分类
    addOrUpdate() {
      if ("add" == this.dialogType) {
        this.addCategory()
      }
      if ("edit" == this.dialogType) {
        this.updateCategory()
      }
    },

    //真正修改分类信息(调用后端程序的接口)
    updateCategory() {
      //1. 注意分析: 这里我们只是提交需要修改的字段信息, 没有提交的字段, 在
      //   数据库中不会被修改(保持原来的值.. 我们可以再观察sql语句)
      //2. var {id, name, icon, proUnit} = this.category 进行对象解构
      var {id, name, icon, proUnit} = this.category
      //调用后端程序的接口/API , 发出修改请求
      this.$http({
        // 请求 url
        url: 'http://localhost:9090/commodity/category/update',
        method: 'post',
        //发出请求时，携带的数据
        data: this.$http.adornData({id, name, icon, proUnit}, false)
      }).then(({data}) => {
        //输出
        this.$message({
          message: '分类信息修改成功OK',
          type: 'success'
        })
        //关闭修改分类的对话框
        this.dialogVisible = false
        //刷新分类列表
        this.getCategories()
        //设置需要展开的菜单
        this.expandedKey = [this.category.parentId]
      })
    },

    //点击Edit按钮, 回显分类信息
    edit(data) {
      console.log("data=> ", data)

      //显示回显对话框-准备修改
      this.dialogType = "edit"
      this.dialogVisible = true

      //发送请求, 到后端程序去获取分类的信息(实时数据,从DB获取)
      this.$http({
        // 请求 url
        url: `http://localhost:9090/commodity/category/info/${data.id}`,
        method: 'get'
      }).then(({data}) => {
        // console.log("返回的data=>", data)
        //将返回的data数据字段信息, 分别绑定到catagory对象[可以有选择性的绑定]
        this.category.name = data.category.name
        this.category.icon = data.category.icon
        this.category.proUnit = data.category.proUnit
        this.category.id = data.category.id
        //为了配合显示展开的菜单, 把this.category.parentId也绑定
        this.category.parentId = data.category.parentId
        //不修改的，可以不用绑定
        //......
      })

    },
    //处理点击添加分类，真正的调用后端的接口,将分类信息入库
    addCategory() {

      this.$http({
        // 请求 url
        url: 'http://localhost:9090/commodity/category/save',
        method: 'post',
        //发出请求时，携带的数据
        data: this.$http.adornData(this.category, false)
      }).then(({data}) => {
        //输出
        this.$message({
          message: '分类信息保存成功OK',
          type: 'success'
        })
        //关闭添加分类的对话框
        this.dialogVisible = false
        //刷新分类列表
        this.getCategories()
        //设置需要展开的菜单
        this.expandedKey = [this.category.parentId]
      })
    },
    append(data) { //处理点击添加分类-显示添加分类的对话框
      //输出data, 观察数据的结构-实际开发也可以这样 -> 讲解是是方法-思路
      console.log("data=>", data)
      //显示添加分类的对话框
      //设置对话框的类型
      this.dialogType = "add"
      this.dialogVisible = true

      this.category.parentId = data.id
      //在添加时,重置了this.category 属性
      this.category.sort = 0
      this.category.proUnit = ""
      this.category.proCount = 0
      this.category.name = ""
      this.category.isShow = 1
      this.category.id = null
      this.category.icon = ""
      //添加的分类为其父类的层级+1, 这里 * 1 是为了将字符串转成数值
      this.category.catLevel = data.catLevel * 1 + 1
      console.log("this.category=>", this.category)
    },
    remove(node, data) { //处理点击删除分类
      //输出node, data 观察数据的结构
      console.log("node=>", node, " data=>", data)
      //这里我们可以参考前面写过的代码来完成删除任务
      //1. 获取删除的分类的id
      var ids = [data.id]

      this.$confirm(`是否删除【${data.name}】菜单?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        //2. 如果点击确认 , 发出请求, 完成删除
        this.$http({
          // 请求 url
          url: 'http://localhost:9090/commodity/category/delete',
          method: 'post',
          //发出请求时，携带的数据
          data: this.$http.adornData(ids, false)
        }).then(({data}) => { //{data} 是解构了返回数据的data部分
          //输出
          //console.log("返回的data= ", data)
          this.$message({
            message: '操作成功',
            type: 'success'
          })
          //刷新分类列表
          this.getCategories()
          //设置需要展开的菜单
          this.expandedKey = [node.parent.data.id]
        })
      }).catch(() => { //点击取消，就不删除了
      })

    },
    // 获取分类列表,带层级
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
    }
  },
  created() { //我们前面讲解vue讲过vue的生命周期..
    this.getCategories();
  }
};
</script>
<style scoped>
</style>
