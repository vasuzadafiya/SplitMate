package com.example.splitpal;

public class Expense {
    private String id;
    private String amount;
    private String date;
    private String category;
    private String paymentMethod;

    // Default constructor required for calls to DataSnapshot.getValue(Expense.class)
    public Expense() {}

    public Expense(String id, String amount, String date, String category, String paymentMethod) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.paymentMethod = paymentMethod;
    }

    public Expense(String amount, String date, String category, String paymentMethod) {
        this.amount=amount;
        this.date=date;
        this.category=category;
        this.paymentMethod=paymentMethod;
    }

    // Getters and setters
    public String getId() {
        return id;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
