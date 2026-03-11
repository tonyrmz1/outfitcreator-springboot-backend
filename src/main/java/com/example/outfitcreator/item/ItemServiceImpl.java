package com.example.outfitcreator.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);
    @Autowired
    ItemRepository itemRepository;

    @Override
    public Item getItem(Long id) {
        Item item = this.itemRepository.findById(id).orElse(new Item());
        log.info(item.getBrand());
        return item;
    }

    @Override
    public List<Item> getAll() {
        return this.itemRepository.findAll();
    }

    @Override
    public Item createItem(Item request) {
        return this.itemRepository.save(request);
    }
}
