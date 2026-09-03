<template>
  <!-- 处理品牌的修改和添加 -->
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()"
             label-width="100px">
      <el-form-item label="品牌名" prop="name">
        <el-input v-model="dataForm.name" placeholder="品牌名"></el-input>
      </el-form-item>
      <el-form-item label="logo" prop="logo">
        <!--        <el-input v-model="dataForm.logo" placeholder="logo"></el-input>-->
        <!-- 在logo项, 使用文件上传组件, 替换原来的el-input -->
        <SingleUpload v-model="dataForm.logo"></SingleUpload>
      </el-form-item>
      <el-form-item label="说明" prop="description">
        <el-input v-model="dataForm.description" placeholder="说明"></el-input>
      </el-form-item>
      <el-form-item label="显示" prop="isshow">
        <el-input v-model="dataForm.isshow" placeholder="显示"></el-input>
      </el-form-item>
      <el-form-item label="检索首字母" prop="firstLetter">
        <el-input v-model="dataForm.firstLetter" placeholder="检索首字母"></el-input>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input v-model="dataForm.sort" placeholder="排序"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
//引入需要使用的控件
import SingleUpload from "@/components/upload/singleUpload"

export default {
  //使用
  components: {
    SingleUpload
  },
  data() {
    return {
      visible: false,
      dataForm: {
        id: 0,
        name: '',
        logo: '',
        description: '',
        isshow: '',
        firstLetter: '',
        sort: ''
      },
      dataRule: {
        name: [
          {required: true, message: '品牌名不能为空', trigger: 'blur'}
        ],
        logo: [
          {required: true, message: 'logo不能为空', trigger: 'blur'}
        ],
        description: [
          {required: true, message: '说明不能为空', trigger: 'blur'}
        ],
        isshow: [
          {required: true, message: '显示不能为空', trigger: 'blur'}
        ],
        firstLetter: [ // 增加自定义检索首字母，必须是a-z 或者 A-Z
          // {required: true, message: '检索首字母不能为空', trigger: 'blur'}
          {
            // 增加一个校验器
            // 老韩一会再做说明~
            /**
             * 老师解读
             * 0. 关于elementUI 的表单验证，参考文档 elementUI-Form 表单-表单验证
             * 1. validator 可以编写自定义的校验器(就是一个方法), 参考文档 elementUI-Form 表单-自定义校验规则
             * 2. 这里我们直接用箭头函数写在 validator: 后面
             * 3. 参数 rule 是规则 value 是用户填写内容 callback是一个回调方法，会将错误信息填写在对应的表单处
             * 4. trigger: 'blur' 表示表单输入框失去输入焦点， 触发校验
             */
            validator: (rule, value, callback) => {
              if (value === '') {
                callback(new Error('检索首字母必须填写'));
              } else if (!/^[a-zA-Z]$/.test(value)) { // 必须是a-z 或者 A-Z
                callback(new Error('检索首字母必须是a-z 或者 A-Z'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
        sort: [ // 排序值sort 必须是大于等于0的整数 - java基础时，讲过正则表达式
          // {required: true, message: '排序不能为空', trigger: 'blur'}
          {
            validator: (rule, value, callback) => {
              if (value === '') {
                callback(new Error('排序值必须填写'));
              } else if (!/^([1-9]\d*|0)$/.test(value)) { // 大于等于0的整数
                callback(new Error('排序值sort 必须是大于等于0的整数'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    init(id) {
      this.dataForm.id = id || 0
      this.visible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        if (this.dataForm.id) {
          this.$http({
            url: this.$http.adornUrl(`/commodity/brand/info/${this.dataForm.id}`),
            // url: `http://localhost:9090/commodity/brand/info/${this.dataForm.id}`,
            method: 'get',
            params: this.$http.adornParams()
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.dataForm.name = data.brand.name
              this.dataForm.logo = data.brand.logo
              this.dataForm.description = data.brand.description
              this.dataForm.isshow = data.brand.isshow
              this.dataForm.firstLetter = data.brand.firstLetter
              this.dataForm.sort = data.brand.sort
            }
          })
        }
      })
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.$http({
            url: this.$http.adornUrl(`/commodity/brand/${!this.dataForm.id ? 'save' : 'update'}`),
            // url: `http://localhost:9090/commodity/brand/${!this.dataForm.id ? 'save' : 'update'}`,
            method: 'post',
            data: this.$http.adornData({
              'id': this.dataForm.id || undefined,
              'name': this.dataForm.name,
              'logo': this.dataForm.logo,
              'description': this.dataForm.description,
              'isshow': this.dataForm.isshow,
              'firstLetter': this.dataForm.firstLetter,
              'sort': this.dataForm.sort
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
