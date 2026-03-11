# Database Indexes and Query Optimization

## Overview

This document describes the database indexes implemented for the OutfitCreator backend application to ensure optimal query performance per requirements 15.1 and 15.2.

## Implemented Indexes

### 1. User Table Indexes

| Index Name | Column(s) | Type | Purpose |
|------------|-----------|------|---------|
| `idx_users_email` | email | Unique | Fast authentication lookups and email uniqueness enforcement |

### 2. ClothingItem Table Indexes

| Index Name | Column(s) | Type | Purpose |
|------------|-----------|------|---------|
| `idx_clothing_items_user_id` | user_id | Non-unique | Fast retrieval of all items for a user (foreign key) |
| `idx_clothing_items_category` | category | Non-unique | Filter items by category (TOP, BOTTOM, FOOTWEAR, etc.) |
| `idx_clothing_items_season` | season | Non-unique | Filter items by season (SPRING, SUMMER, AUTUMN, WINTER) |
| `idx_clothing_items_primary_color` | primary_color | Non-unique | Filter items by color for recommendation engine |

### 3. Outfit Table Indexes

| Index Name | Column(s) | Type | Purpose |
|------------|-----------|------|---------|
| `idx_outfits_user_id` | user_id | Non-unique | Fast retrieval of all outfits for a user (foreign key) |

### 4. OutfitItem Table Indexes

| Index Name | Column(s) | Type | Purpose |
|------------|-----------|------|---------|
| `idx_outfit_items_outfit_id` | outfit_id | Non-unique | Fast retrieval of items in an outfit (foreign key) |
| `idx_outfit_items_clothing_item_id` | clothing_item_id | Non-unique | Check if clothing item is used in outfits (foreign key) |

## Query Optimization

### Common Query Patterns

1. **User Authentication**
   ```sql
   SELECT * FROM users WHERE email = ?
   ```
   - Uses: `idx_users_email`
   - Expected performance: < 10ms

2. **Retrieve User's Clothing Items**
   ```sql
   SELECT * FROM clothing_items WHERE user_id = ?
   ```
   - Uses: `idx_clothing_items_user_id`
   - Expected performance: < 500ms for collections under 1000 items (Requirement 15.1)

3. **Filter by Category**
   ```sql
   SELECT * FROM clothing_items WHERE user_id = ? AND category = ?
   ```
   - Uses: `idx_clothing_items_user_id`, `idx_clothing_items_category`
   - Expected performance: < 500ms

4. **Filter by Season**
   ```sql
   SELECT * FROM clothing_items WHERE user_id = ? AND season = ?
   ```
   - Uses: `idx_clothing_items_user_id`, `idx_clothing_items_season`
   - Expected performance: < 500ms

5. **Filter by Color**
   ```sql
   SELECT * FROM clothing_items WHERE user_id = ? AND primary_color = ?
   ```
   - Uses: `idx_clothing_items_user_id`, `idx_clothing_items_primary_color`
   - Expected performance: < 500ms

6. **Complex Filter (Category + Season)**
   ```sql
   SELECT * FROM clothing_items 
   WHERE user_id = ? AND category = ? AND season = ?
   ```
   - Uses: `idx_clothing_items_user_id`, `idx_clothing_items_category`, `idx_clothing_items_season`
   - Expected performance: < 500ms

7. **Retrieve User's Outfits**
   ```sql
   SELECT * FROM outfits WHERE user_id = ?
   ```
   - Uses: `idx_outfits_user_id`
   - Expected performance: < 500ms

8. **Check if Item is in Outfits**
   ```sql
   SELECT COUNT(*) > 0 FROM outfit_items WHERE clothing_item_id = ?
   ```
   - Uses: `idx_outfit_items_clothing_item_id`
   - Expected performance: < 100ms

## Performance Requirements

Per Requirement 15.1 and 15.2:
- Retrieving a list of ClothingItems SHALL respond within 500ms for collections under 1000 items
- Generating outfit recommendations SHALL respond within 2 seconds for typical DigitalCloset sizes

## Index Verification

To verify indexes are created correctly in PostgreSQL:

```sql
-- List all indexes on a table
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'clothing_items';

-- Check index usage statistics
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
WHERE tablename IN ('users', 'clothing_items', 'outfits', 'outfit_items');
```

## Query Hints

For the current implementation, no explicit query hints are needed as:
1. All foreign keys are indexed
2. All filter columns (category, season, color) are indexed
3. PostgreSQL query planner automatically selects appropriate indexes
4. Query patterns are straightforward and don't require complex optimization

If performance issues arise in production with large datasets, consider:
- Adding composite indexes for frequently combined filters
- Analyzing query execution plans with `EXPLAIN ANALYZE`
- Adjusting PostgreSQL configuration parameters (shared_buffers, work_mem, etc.)

## Maintenance

### Index Monitoring

Monitor index usage regularly:
```sql
-- Find unused indexes
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0
AND indexname NOT LIKE 'pg_toast%';
```

### Index Rebuilding

Indexes may need rebuilding after significant data changes:
```sql
REINDEX TABLE clothing_items;
REINDEX TABLE outfits;
REINDEX TABLE outfit_items;
```

## Testing

Performance tests are located in:
- `src/test/java/com/example/outfitcreator/performance/DatabasePerformanceTest.java`

These tests verify:
- Query performance with large datasets (1000+ items)
- Index effectiveness for filtering operations
- Pagination performance
- Complex query performance

Run performance tests with:
```bash
mvn test -Dtest=DatabasePerformanceTest
```

## Implementation Details

Indexes are defined using JPA annotations in entity classes:

```java
@Entity
@Table(name = "clothing_items",
        indexes = {
            @Index(name = "idx_clothing_items_user_id", columnList = "user_id"),
            @Index(name = "idx_clothing_items_category", columnList = "category"),
            @Index(name = "idx_clothing_items_season", columnList = "season"),
            @Index(name = "idx_clothing_items_primary_color", columnList = "primary_color")
        })
public class ClothingItem {
    // ...
}
```

Hibernate automatically creates these indexes during schema generation.
