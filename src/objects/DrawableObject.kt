package objects

import annotation.CObject
import com.jogamp.opengl.GL
import annotation.CProperty
import com.jogamp.opengl.GL2
import ui.GlCanvas

/**
 * 描画が発生する小向ジェクトの親クラス
 * 座標、拡大率、透明度などをもつ
 */
@CObject("描画")
abstract class DrawableObject:CitrusObject() {
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


    open fun onDraw(gl: GL2, mode : DrawMode){
        if(mode == DrawMode.Preview && enabledSelectedOutline && selected){

        }
    }
    override fun onFrame() {
        onDraw(GlCanvas.gl2,DrawMode.Preview)
    }
}