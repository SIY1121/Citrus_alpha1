package properties

class TextProperty(def : String = "テキストを入力") {
    var text = def
        set(value){
            field = value
            listener?.onChanged(field)
        }
    interface ChangeListener{
        fun onChanged(text : String)
    }
    var listener : ChangeListener? = null
}