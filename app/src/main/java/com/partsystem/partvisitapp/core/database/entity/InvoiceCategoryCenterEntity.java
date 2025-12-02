package com.partsystem.partvisitapp.core.database.entity;

import androidx.room.Entity;

import java.io.Serializable;

@Entity(tableName = "invoice_category_center_table")
public class InvoiceCategoryCenterEntity implements Serializable {
    public Integer InvoiceCategoryId;

    public Integer CenterId;
}