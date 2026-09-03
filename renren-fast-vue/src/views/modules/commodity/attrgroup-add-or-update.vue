<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    @close="dialogClose"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()"
             label-width="80px">
      <el-form-item label="组名" prop="name">
        <el-input v-model="dataForm.name" placeholder="组名"></el-input>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input v-model="dataForm.sort" placeholder="排序"></el-input>
      </el-form-item>
      <el-form-item label="说明" prop="description">
        <el-input v-model="dataForm.description" placeholder="说明"></el-input>
      </el-form-item>
      <el-form-item label="组图标" prop="icon">
        <el-input v-model="dataForm.icon" placeholder="组图标"></el-input>
      </el-form-item>
      <el-form-item label="所属分类id" prop="categoryId">
        <!--        <el-input v-model="dataForm.categoryId" placeholder="所属分类id"></el-input>-->
        <!--
            老师解读:
              1. 在添加选择所属分类ID时，我们使用了 Cascader 级联选择器, 具体用法参考 elementui-Cascader 级联选择器-基础用法
              2. v-model="cascadedCategoryId" : 是最终绑定的值,因为el-cascader 关联的v-model 是一个数组，
                 记录的是三级分类全部id,即[第1级分类id,第2级分类id,第3级分类id], 比如[1,21,301]因此在数据池的categoryId是一个数组
              3. :options="categories" 表示级联显示的时候各个层的选值是从categories数组来的
              4. :props="props" 显示的选项值(value)/显示的标签(label)/子选项(children) 分别和返回的category的对象的哪个字段关联
              5. filterable placeholder="搜索: " 在级联选择器，增加搜索分类的功能
        -->
        <el-cascader
          filterable
          placeholder="搜索: "
          v-model="cascadedCategoryId"
          :options="categories"
          :props="props"
        ></el-cascader>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      categories: [], //层级关系的分类
      cascadedCategoryId: [],//[1,21,301]
      props: { //显示级联菜单的属性指定
        value: "id",
        label: "name",
        children: "childrenCategories"
      },
      visible: false,
      dataForm: {
        id: 0,
        name: '',
        sort: '',
        description: '',
        icon: '',
        categoryId: 0 // 默认给categoryId=0, 这里前端人员可以根据自己的需求来处理
      },
      dataRule: {
        name: [
          {required: true, message: '组名不能为空', trigger: 'blur'}
        ],
        sort: [
          {required: true, message: '排序不能为空', trigger: 'blur'}
        ],
        description: [
          {required: true, message: '说明不能为空', trigger: 'blur'}
        ],
        icon: [
          {required: true, message: '组图标不能为空', trigger: 'blur'}
        ],
        categoryId: [
          {required: true, message: '所属分类id不能为空', trigger: 'blur'}
        ]
      }
    }
  },
  created() {
    this.getCategories() //第一次进入到该页面,就调用方法.
  },
  methods: {
    // 增加方法，清空this.cascadedCategoryId
    dialogClose() {
      this.cascadedCategoryId = []
    },
    // 增加方法, 返回所有的分类信息,获取分类列表,带层级
    getCategories() {
      this.$http({
        // 请求 url
        url: this.$http.adornUrl('/commodity/category/list/tree'),
        method: 'get'
      }).then(({data}) => { //{data} 是解构了返回数据的data部分
        //输出
        console.log("返回的data= ", data)
        // 将返回的层级关系的数据和this.categories绑定
        this.categories = data.data
      })
    },
    init(id) {
      // 因为级联选择器，在展示分类层级时，依据 cascadedCategoryId
      // console.log("cascadedCategoryId->", this.cascadedCategoryId)
      this.dataForm.id = id || 0
      this.visible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        if (this.dataForm.id) {
          this.$http({
            url: this.$http.adornUrl(`/commodity/attrgroup/info/${this.dataForm.id}`),
            method: 'get',
            params: this.$http.adornParams()
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.dataForm.name = data.attrgroup.name
              this.dataForm.sort = data.attrgroup.sort
              this.dataForm.description = data.attrgroup.description
              this.dataForm.icon = data.attrgroup.icon
              this.dataForm.categoryId = data.attrgroup.categoryId
              // 为了实现每次点击修改，能够对应显示分类层级
              // 需要获取到对应的this.cascadedCategoryId,就OK
              this.cascadedCategoryId = data.attrgroup.cascadedCategoryId
            }
          })
        }
      })
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        // 老师输入 cascadedCategoryId 值
        // console.log("cascadedCategoryId=", this.cascadedCategoryId)
        // this.dataForm.categoryId = this.cascadedCategoryId[this.cascadedCategoryId.length - 1]
        if (valid) {
          this.$http({
            url: this.$http.adornUrl(`/commodity/attrgroup/${!this.dataForm.id ? 'save' : 'update'}`),
            method: 'post',
            data: this.$http.adornData({
              'id': this.dataForm.id || undefined,
              'name': this.dataForm.name,
              'sort': this.dataForm.sort,
              'description': this.dataForm.description,
              'icon': this.dataForm.icon,
              // 'categoryId': this.dataForm.categoryId
              // 说明: this.cascadedCategoryId 数组保存了当前用户选择的分类的层级关系
              // 形式为 [1, 22, 202], 我们要提交的 categoryId就是数组的最后元素的值
              'categoryId': this.cascadedCategoryId[this.cascadedCategoryId.length - 1]
            })
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: '操作成功',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.visible = false
                  this.$emit('refreshDataList')
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        }
      })
    }
  }
}
</script>
