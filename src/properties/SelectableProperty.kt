package properties

/**
 * 選択式のプロパティ
 * アノテーションをつけて使う
 * 実画面ではドロップダウンボックスで表示される
 * @param list 選択アイテム
 */
class SelectableProperty(val list : List<String>) {
    var selectedIndex = 0
        set(value){
            field=value
            listener?.onChanged(field)
        }
    interface ChangeListener{
        fun onChanged(index : Int)
    }
    var listener: ChangeListener? = null
}