package com.partsystem.partvisitapp.core.utils

object Constant {

    const val BASE_URL = "http://hf2093f87sf.sn.mynetname.net:52438/api/Android/"
}

enum class SnackBarType(val value: String) {
    Error("error"),
    Success("success"),
    Warning("warning"),
}

enum class ReportFactorListType(val value: String) {
    Customer("customer"),
    Visitor("visitor")
}

enum class OrderType(val value: String) {
    Add("add"),
    Edit("edit")
}

enum class ImageProductType(val value: String) {
    GROUP_PRODUCT("Store.GroupProduct"), PRODUCT("Store.Product")
}

enum class ActKind(val value: String) {
    None("none"),

    // [Description("خدمات")]
    Service("service"),

    //[Description("کالا")]
    Product("product"),

    //        [Description("خرید خدمات")]
    BoughtService("boughtService"),

    //        [Description("خرید کالا")]
    BoughtProduct("boughtProduct"),
}

enum class FactorFormKind {
    Unknown,

    //"صورتحساب فروش/خدمات"*/
    Factor,

    /***
     * "برگشت از فروش/خدمات"
     */
    BackFactor,

    /***
     * "پیش فاکتور"
     */
    PishFactor,

    /***
     * "متمم بدهکار"
     */
    MotamemBedehkar,

    /***
     * "متمم بستانکار"
     */
    MotamemBestankar,

    /***
     * "ثبت سفارش"
     */
    RegisterOrder,

    /***
     * "اصلاح سفارش"
     */
    ModifyOrder,  //فروشگاهی

    /***
     * "فاکتور فروشگاهی"
     */
    ShopFactor,

    /***
     * "برگشت فاکتور فروشگاهی"
     */
    BackShopFactor,

    /***
     * "پیش فروش"
     */
    PishForoosh,

    /***
     * "درخواست برگشت از فروش"
     */
    RequestBack,  //توزیع

    /***
     * "فاکتور فروش توزیع"
     */
    FactorDistribute,

    /***
     * "برگشت از فروش توزیع"
     */
    BackFactorDistribute,

    /***
     * "پیش فاکتور توزیع"
     */
    PishFactorDistribute,

    /***
     * "متمم بدهکار توزیع"
     */
    MotamemBedehkarDistribute,

    /***
     * "متمم بستانکار توزیع"
     */
    MotamemBestankarDistribute,

    /***
     * "ثبت سفارش توزیع"
     */
    RegisterOrderDistribute
}

enum class SaleRateKind {
    Pattern,
    Act,
    None
}

enum class CalculateUnit2Type {
    //        [Description("میانگین واحد اول بر اساس واحد دوم")]
    AverageUnits,

    //        [Description("فرمول استاندارد")]
    StandardFormula,

    //        [Description("توسط کاربر")]
    Manual
}
