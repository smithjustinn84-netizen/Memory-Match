---
trigger: model_decision
description: Used when adding an item, skin, theme, ect to the shop for the player to buy
---

# 💰 Shop Economy Rule

This rule ensures that the game economy remains balanced when adding new items, skins, or themes to the shop.

## Pricing Tiers

When adding new items, strictly follow these pricing tiers to maintain the intended progression speed:

| Tier                 | Item Examples                            | Price | "Casual" Games to Afford |
| -------------------- | ---------------------------------------- | ----- | ------------------------ |
| **Tier 1: Standard** | Basic colors, standard geometric themes  | 400   | ~4 Games                 |
| **Tier 2: Premium**  | Pattern themes, Minimal/Poker card faces | 800   | ~8 Games                 |
| **Tier 3: Luxury**   | Gold, Executive, Gilded, Midnight themes | 2000  | ~20 Games                |

## Payout Baseline

The pricing is balanced against a **Casual** payout baseline of **~100 currency per game**.

- If an item should be harder to get, increase the tier.
- If an item is a simple variation, keep it in Tier 1.

## Trigger

This rule triggers when:
1. Adding new entries to `shop_items.json`.
2. Modifying `ScoringCalculator.kt`.
3. Adding new `CardBackTheme` or `CardSymbolTheme` variations.
