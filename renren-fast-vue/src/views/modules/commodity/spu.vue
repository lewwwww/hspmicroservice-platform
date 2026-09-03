<template>
  <div>
    <el-row>
      <el-col :span="24">
        <el-form :inline="true" :model="dataForm">
          <el-form-item label="分类">
            <!--            <category-cascader :catelogPath.sync="catelogPath"></category-cascader>-->
            <!-- 这里我们加入分类的Cascader 级联选择器, 前面我们使用过 -->
            <el-cascader :cascadedCategoryId.sync="cascadedCategoryId" v-model="cascadedCategoryId" :options="categorys"
                         :props="props"></el-cascader>
          </el-form-item>
          <el-form-item label="品牌">
            <!--            <brand-select style="width:160px"></brand-select>-->
            <el-select placeholder="请选择" :brandId.sync="brandId" v-model="brandId" filterable clearable>
              <el-option
                v-for="item in brands"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select style="width:160px" v-model="dataForm.status" clearable>
              <el-option label="新建" :value="0"></el-option>
              <el-option label="上架" :value="1"></el-option>
              <el-option label="下架" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="检索">
            <el-input style="width:160px" v-model="dataForm.key" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchSpuInfo">查询</el-button>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="24">
        <spuinfo :catId="catId"></spuinfo>
      </el-col>
    </el-row>
  </div>
</template>

<script>

// import CategoryCascader from "../common/category-cascader";
// import BrandSelect from "../common/brand-select";
import Spuinfo from "./spuinfo";

export default {
  //import引入的组件需要注入到对象中才能使用
  // components: { CategoryCascader, Spuinfo, BrandSelect },
  components: {Spuinfo},
  props: {},
  data() {
    //这里存放数据
    return {

      //品牌选择下拉相关数据变量 start
      categoryId: 0,
      brands: [
        {
          label: "a",
          value: 1
        }
      ],

      brandId: "",
      subscribe: null,
      //品牌选择下拉相关数据变量 end

      //集成分类层级控件， 相关数据
      cascadedCategoryId: [],
      props: {      //显示返回的家居分类的哪些字段/信息
        value: "id",
        label: "name",
        children: "childrenCategories"
      },
      categorys: [], //所有的家居分类

      catId: 0,
      catalogPath: [],
      dataForm: {
        status: "",
        key: "",
        brandId: 0,
        catalogId: 0
      },
      catPathSub: null,
      brandIdSub: null

    };
  },
  //计算属性类似于data概念
  computed: {},
  //监控data中的数据变化
  watch: {
    //如果三级分类变化，就做处理
    cascadedCategoryId(path) {
      console.log("三级分类变化了~", path);
      this.categoryId = path[path.length - 1];
      this.dataForm.catalogId = this.categoryId //修改检索条件的 catalogId
      //把当前的分类id，也赋给this.spu.catalogId , 后面在请求分类下的属性组和属性有用
      //this.spu.catalogId = this.categoryId;
      this.getCatBrands();
    },

    //监控品牌id的变化, 如果变化就赋给this.dataForm, 后面检索需要
    brandId(val) {
      this.brandId = val;
      this.dataForm.catalogId = this.categoryId;
      this.dataForm.brandId = val;
    }

  },
  //方法集合
  methods: {

    //获取到某个分类的品牌
    getCatBrands() {
      this.$http({
        //url: "http://localhost:9090/commodity/categorybrandrelation/brands/list",
        url: this.$http.adornUrl("/commodity/categorybrandrelation/brands/list"),
        method: "get",
        params: this.$http.adornParams({
          catId: this.categoryId
        })
      }).then(({data}) => {
        this.brands = data.data;
      });
    },

    //显示关联品牌和分类的相关方法
    getCategorys() {
      this.$http({
        //url: "http://localhost:9090/commodity/category/list/tree",
        url: this.$http.adornUrl("/commodity/category/list/tree"),
        method: "get"
      }).then(({data}) => {
        this.categorys = data.data;
      });
    },

    searchSpuInfo() {
      console.log("搜索条件", this.dataForm);
      this.PubSub.publish("dataForm", this.dataForm);
    }
  },
  //生命周期-创建完成（可以访问当前this实例）
  created() {
    this.getCategorys();
  },
  //生命周期-挂载完成（可以访问DOM元素）
  mounted() {
    // this.catPathSub = PubSub.subscribe("catPath", (msg, val) => {
    //   this.dataForm.catelogId = val[val.length-1];
    // });
    // this.brandIdSub = PubSub.subscribe("brandId", (msg, val) => {
    //   this.dataForm.brandId = val;
    // });
  },
  beforeCreate() {
  }, //生命周期-创建之前
  beforeMount() {
  }, //生命周期-挂载之前
  beforeUpdate() {
  }, //生命周期-更新之前
  updated() {
  }, //生命周期-更新之后
  beforeDestroy() {
    // PubSub.unsubscribe(this.catPathSub);
    // PubSub.unsubscribe(this.brandIdSub);
  }, //生命周期-销毁之前
  destroyed() {
  }, //生命周期-销毁完成
  activated() {
  } //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>
<style scoped>
</style>
