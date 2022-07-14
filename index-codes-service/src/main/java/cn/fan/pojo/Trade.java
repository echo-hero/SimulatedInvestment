package cn.fan.pojo;

public class Trade {

    public String getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(String buyDate) {
        this.buyDate = buyDate;
    }

    public String getSellDate() {
        return sellDate;
    }

    public void setSellDate(String sellDate) {
        this.sellDate = sellDate;
    }

    public double getBuyClosePoint() {
        return buyClosePoint;
    }

    public void setBuyClosePoint(double buyClosePoint) {
        this.buyClosePoint = buyClosePoint;
    }

    public double getSellClosePoint() {
        return sellClosePoint;
    }

    public void setSellClosePoint(double sellClosePoint) {
        this.sellClosePoint = sellClosePoint;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    private String buyDate;//购买日期
    private String sellDate;//卖出日期
    private double buyClosePoint;//买入金额
    private double sellClosePoint;//出售金额
    private double rate;//收益
}
