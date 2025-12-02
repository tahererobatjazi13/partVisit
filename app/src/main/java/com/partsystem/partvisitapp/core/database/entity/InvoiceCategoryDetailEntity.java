package com.partsystem.partvisitapp.core.database.entity;

import androidx.room.Entity;
import java.io.Serializable;

@Entity(tableName = "invoice_category_detail_table")
public class InvoiceCategoryDetailEntity implements Serializable {
    public Integer InvoiceCategoryId;

    public Integer UserId;
}
