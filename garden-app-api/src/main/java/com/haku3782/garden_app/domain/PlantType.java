package com.haku3782.garden_app.domain;

/**
 * 植物の種別を表す列挙型（Enum）。
 *
 * <p>Plant エンティティの type フィールドで使用する。
 * DB には @Enumerated(EnumType.STRING) により文字列として保存される。
 *
 * <p>【種別一覧】
 * <ul>
 *   <li>vegetable  : 野菜</li>
 *   <li>fruit      : 果物</li>
 *   <li>herb       : ハーブ</li>
 *   <li>flower     : 花</li>
 *   <li>tree       : 樹木</li>
 *   <li>houseplant : 観葉植物</li>
 *   <li>succulent  : 多肉植物</li>
 *   <li>airplant   : エアプランツ</li>
 *   <li>other      : その他</li>
 * </ul>
 */
public enum PlantType {
    /** 野菜 */
    vegetable,
    /** 果物 */
    fruit,
    /** ハーブ */
    herb,
    /** 花 */
    flower,
    /** 樹木 */
    tree,
    /** 観葉植物 */
    houseplant,
    /** 多肉植物 */
    succulent,
    /** エアプランツ */
    airplant,
    /** その他 */
    other
}
