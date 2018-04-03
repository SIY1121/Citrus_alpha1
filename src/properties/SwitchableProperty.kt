package properties

/**
 *On Offが可能なプロパティ
 * @param def 初期値
 */
class SwitchableProperty(def : Boolean = false) {
    var value = def
        set(value){
            field = value
            listener?.onChanged(field)
        }

    interface ChangeListener{
        fun onChanged(value : Boolean)
    }

    var listener : ChangeListener? = null
}