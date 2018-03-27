package objects

/**
 * タイムラインに並ぶオブジェクトのスーパークラス
 */
open class CitrusObject {
    open val id = "citrus"
    open val name = "CitrusObject"
    var displayName = "CitrusObject"
        set(value){
            field = value
            displayNameChangeListener?.onDisplayNameChanged(value)
        }
    interface DisplayNameChangeListener{
        fun onDisplayNameChanged(name : String)
    }
    var displayNameChangeListener : DisplayNameChangeListener? = null



    var start : Int = 0
    var end : Int = 1
    var layer : Int = 1
}