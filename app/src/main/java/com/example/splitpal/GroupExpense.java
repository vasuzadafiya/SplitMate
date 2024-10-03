package com.example.splitpal;

public class GroupExpense {
    private String id,amount,date,category,paidBy;

    public GroupExpense() {}

    public GroupExpense(String id, String amount, String date, String category, String paidBy) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.paidBy = paidBy;
    }
    public GroupExpense( String amount, String date, String category, String paidBy) {
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.paidBy = paidBy;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }
}
