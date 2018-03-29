package objects

import com.sun.xml.internal.fastinfoset.util.StringArray

/**
 * 選択式のプロパティ
 * アノテーションをつけて使う
 * 実画面ではドロップダウンボックスで表示される
 * @param list 選択アイテム
 */
class SelectableProperty(val list : List<String>) {
    var selectedIndex = 0
}