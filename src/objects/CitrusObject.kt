package objects

import util.Statics

/**
 * タイムラインに並ぶオブジェクトのスーパークラス
 * 格納先配列へのバインディング実装済み
 */
abstract class CitrusObject{

    open val id = "citrus"
    open val name = "CitrusObject"
    var displayName = "CitrusObject"
        set(value) {
            field = value
            displayNameChangeListener?.onDisplayNameChanged(value)
        }

    var frame:Int = 0
        private set

    interface DisplayNameChangeListener {
        fun onDisplayNameChanged(name: String)
    }

    var displayNameChangeListener: DisplayNameChangeListener? = null

    /**
     * タイムラインで動かされ終わった時に呼び出される
     */
    open fun onLayoutUpdate() {

    }

    fun onSuperFrame(frameInVideo :Int){
        frame = frameInVideo - start
        onFrame()
    }

    protected abstract fun onFrame()

    fun isActive(frame : Int) = (frame in start..(end - 1))

    var start: Int = 0
        set(value){
            field = value
            //Statics.project.Layer[layer].sortBy { it.start }
            //Todo ソートは保留
        }
    var end: Int = 1
    var layer: Int = -1
        set(value) {
            //変更された場合
            if (field != value) {

                //初回ではない場合は移動前のレイヤーに残っているはずなので消す
                if (field != -1)
                    Statics.project.Layer[field].remove(this)

                Statics.project.Layer[value].add(this)
                //Statics.project.Layer[value].sortBy { it.start }
                //TODO ソートは保留

                //field = value
            }

            field = value


        }
}