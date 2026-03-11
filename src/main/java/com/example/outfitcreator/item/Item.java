package com.example.outfitcreator.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "item")
@Data
public class Item {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String brand;
    private String color;
    private String urlImage;
    private String category;
    private String tag;

    public Item() {
    }

    public Item(String name, String brand, String color, String urlImage, String tag) {
        this.name = name;
        this.brand = brand;
        this.color = color;
        this.urlImage = urlImage;
        this.tag = tag;
    }


}
