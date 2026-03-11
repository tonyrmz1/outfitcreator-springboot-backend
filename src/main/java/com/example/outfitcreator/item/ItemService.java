package com.example.outfitcreator.item;

import java.util.List;

public interface ItemService {

    Item getItem(Long id);
    List<Item> getAll();
    Item createItem(Item request);
}
