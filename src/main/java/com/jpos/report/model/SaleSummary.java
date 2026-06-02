package com.jpos.report.model;

public class SaleSummary {
    private final double totalRevenue;
    private final double totalProfit;
    private final double totalCost;

    public SaleSummary(double totalRevenue, double totalProfit, double totalCost) {
        this.totalRevenue = totalRevenue;
        this.totalProfit = totalProfit;
        this.totalCost = totalCost;
    }

    public SaleSummary accumulate(SaleSummary other) {
        return new SaleSummary(
                this.totalRevenue + other.totalRevenue,
                this.totalProfit + other.totalProfit,
                this.totalCost + other.totalCost
        );
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getTotalCost() {
        return totalCost;
    }
}
