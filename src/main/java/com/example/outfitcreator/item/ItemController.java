package com.example.outfitcreator.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;

@Controller
@RequestMapping("api/item")
public class ItemController {


    @Autowired
    private ItemService itemService;



    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id){
        Item item = this.itemService.getItem(id);
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item>createItem(@RequestBody Item request) {
        Item item = this.itemService.createItem(request);
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> items = this.itemService.getAll();
        return new ResponseEntity<>(items,HttpStatus.OK);
    }

}
