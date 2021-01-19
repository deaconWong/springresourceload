package deacon.test.spb.enums;

/**
 * 组件热加载枚举
 * 
 * @author yf.wanglm
 * @date 2020/06/15
 */
public enum ComponentEnums {

    TEST("anche-jiance-test"),
    
    API("anche-jiance-api"),

    CHECKEND("anche-jiance-checkend"),

    DATAMANAGE("anche-jiance-datamanage"),

    DISPATCH("anche-jiance-dispatch"),

    FEE("anche-jiance-fee"),

    MODELS("anche-jiance-models"),

    PROXY("anche-jiance-proxy"),

    REPORT("anche-jiance-report"),

    SELECT("aanche-jiance-select"),

    STATISTICS("anche-jiance-statistics"),

    VEHICLELOGIN("anche-jiance-vehiclelogin");

    String componentName;

    ComponentEnums(String componentName) {
        this.componentName = componentName;
    }

    public String getValue() {
        return this.componentName;
    }
}
