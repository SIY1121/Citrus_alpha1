package objects

import annotation.CObject
import com.jogamp.opengl.GL
import annotation.CProperty

/**
 * 描画が発生する小向ジェクトの親クラス
 * 座標、拡大率、透明度などをもつ
 */
@CObject("描画")
open class DrawableObject:CitrusObject() {
    /**
     * 描画モード
     */
    enum class DrawMode{
        Preview,Final
    }

    var selected : Boolean = false
    var enabledSelectedOutline : Boolean = true

    @CProperty("X",0)
    val x = MutableProperty()
    @CProperty("Y",1)
    val y = MutableProperty()
    @CProperty("Z",2)
    val z = MutableProperty()

    @CProperty("拡大率",3)
    val scale = MutableProperty()
    @CProperty("透明度",4)
    val alpha = MutableProperty()
    @CProperty("回転",5)
    val rotate = MutableProperty()


    open fun onDraw(gl: GL, mode : DrawMode){
        if(mode == DrawMode.Preview && enabledSelectedOutline && selected){

        }
    }

}