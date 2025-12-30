package com.partsystem.partvisitapp.core.utils

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



enum class FactorFormKind {
    Unknown,

    // صورتحساب فروش/خدمات
    Factor,

    // برگشت از فروش/خدمات
    BackFactor,

    //پیش فاکتور
    PishFactor,

    //متمم بدهکار
    MotamemBedehkar,

    //متمم بستانکار 
    MotamemBestankar,

    //ثبت سفارش
    RegisterOrder,

    //اصلاح سفارش
    ModifyOrder,

    //فاکتور فروشگاهی
    ShopFactor,

    //برگشت فاکتور فروشگاهی
    BackShopFactor,

    //پیش فروش
    PishForoosh,

    //درخواست برگشت از فروش
    RequestBack,  //توزیع

    // فاکتور فروش توزیع
    FactorDistribute,

    //برگشت از فروش توزیع
    BackFactorDistribute,

    //پیش فاکتور توزیع
    PishFactorDistribute,

    //متمم بدهکار توزیع
    MotamemBedehkarDistribute,

    //متمم بستانکار توزیع
    MotamemBestankarDistribute,

    // ثبت سفارش توزیع
    RegisterOrderDistribute
}



/** <summary>
 * نحوه تعیین نرخ فروش
</summary> */
enum class SaleRateKind {
    Pattern,
    Act,
    None
}


/** <summary>
 * نوع سناریو بودجه
</summary> */
enum class FactorSettlementKind {
    //        [Description("نقدی")]
    Cash,

    //        [Description("نقدی در سررسید")]
    MaturityCash,

    //        [Description("نقد و اسناد")]
    SanadAndCash,

    //        [Description("اسناد")]
    Sanad,

    //        [Description("اعتباری")]
    Credit,
}


enum class PatternInclusionKind {
    //        [Description("تمامی")]
    All,

    //        [Description("لیست")]
    List
}

enum class DiscountInclusionKind {
    //        [Description("تمامی کالاها/خدمات")]
    All,

    //        [Description("گروه")]
    Group,

    //        [Description("لیست")]
    List,

    //        [Description("نوع کالا")]
    ProductKind
}


/** <summary>
 * انواع تخفیفات/اضافات
</summary> */
enum class DiscountKind {
    //        [Description("تخفیفات")]
    Discount,

    //        [Description("اضافات")]
    Addition,

    //        [Description("کسورات")]
    Deduction,  //[Description("اشانتیون")]
    //Gift = 3,
}

/** <summary>
 * روش اعمال تخفیف
</summary> */
enum class DiscountApplyKind {
    //        [Description("در سطح فاکتور")]
    FactorLevel,

    //        [Description("در سطح ردیف کالا")]
    ProductLevel,

    //        [Description("محاسبات خارج فاکتور")]
    OutsideFactor
}


/** <summary>
 * نوع مبلغ
</summary> */
enum class DiscountPriceKind {
    //        [Description("مبلغ فروش")]
    SalePrice,

    //        [Description("مبلغ فروش پس از کسورات")]
    DiscountedPrice,

    //        [Description("مبلغ خالص فروش")]
    PurePrice,

    //        [Description("مبلغ قابل پرداخت")]
    FinalPrice
}

/** <summary>
 * روش محاسبه تخفیف
</summary> */
enum class DiscountCalculationKind {
    //        [Description("ساده")]
    Simple,

    //        [Description("پلکانی براساس مبلغ")]
    Stair,

    //        [Description("اشانتیون")]
    Eshantyun,

    //        [Description("تاریخ تسویه")]
    SettlementDate,

    //        [Description("تنوع کالا")]
    ProductKind,

    //        [Description("رند")]
    Round,

    //        [Description("جایزه")]
    Gift,

    //        [Description("پلکانی براساس مقدار")]
    StairByValue,

    //        [Description("تخفیف به نسبت مقدار")]
    DiscountByValue,

    //        [Description("تخفیف به نسبت مبلغ")]
    DiscountByPrice,

    // [Description("رند نهایی")]
    FinalRound,

    //[Description("تاریخ تصفیه پلکانی")]
    StairByDate
}

/** <summary>
 * نحوه پرداخت
</summary> */
enum class DiscountPaymentKind {
    //        [Description("درصد")]
    Percent,

    //        [Description("مبلغ")]
    Price,


    //[Description("مبلغ واحد")]
    UnitPrice //[Description("مبلغ رند")]
    //RoundPrice = 3,
}


enum class DiscountUnitKind {
    //        [Description("واحد اول")]
    Unit1,

    //        [Description("واحد دوم")]
    Unit2,

    //        [Description("بسته بندی")]
    Packing
}

/** <summary>
 * نحوه اجرا تخفیفات
</summary> */
enum class DiscountExecuteKind {
    //   [Description("ساده")]
    Simple,

    // [Description("ضریبی")]
    Ratio
}


/** <summary>
 * نحوه محاسبه واحد دوم
 * تعریف کالا
</summary> */
enum class CalculateUnit2Type {
    //        [Description("میانگین واحد اول بر اساس واحد دوم")]
    AverageUnits,

    //        [Description("فرمول استاندارد")]
    StandardFormula,

    //        [Description("توسط کاربر")]
    Manual
}

/** <summary>
 * انتخاب واحد اندازه گیری
</summary> */
enum class UnitKind {
    //        [Description("واحد 1")]
    Unit1,

    //        [Description("واحد 2")]
    Unit2
}

/** <summary>
 * نوع برگه مصوبه
</summary> */
enum class ActKind {
    None,

    // [Description("خدمات")]
    Service,

    //[Description("کالا")]
    Product,

    //        [Description("خرید خدمات")]
    BoughtService,

    //        [Description("خرید کالا")]
    BoughtProduct,
}