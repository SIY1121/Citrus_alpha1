package objects

import util.Statics

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

    /**
     * タイムラインで動かされ終わった時に呼び出される
     */
    open fun onLayoutUpdate(){

    }

    var start : Int = 0
    var end : Int = 1
    var layer : Int = -1
        set(value) {
            //変更された場合
            if(field!=value){

                //初回ではない場合は移動前のレイヤーに残っているはずなので消す
                if(field!=-1)
                    Statics.project.Layer[field].remove(this)

                Statics.project.Layer[value].add(this)

                field = value
            }
        }
}