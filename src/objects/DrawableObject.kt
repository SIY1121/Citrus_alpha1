package objects

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2GL3.GL_LINE
import com.jogamp.opengl.GL2GL3.GL_LINE_LOOP

/**
 * 描画が発生する小向ジェクトの親クラス
 * 座標、拡大率、透明度などをもつ
 */
open class DrawableObject:CitrusObject() {
    /**
     * 描画モード
     */
    enum class DrawMode{
        Preview,Final
    }

    var selected : Boolean = false
    var enabledSelectedOutline : Boolean = true

    val x = MutableProperty()
    val y = MutableProperty()
    val z = MutableProperty()

    val scale = MutableProperty()
    val alpha = MutableProperty()
    val rotate = MutableProperty()


    open fun onDraw(gl: GL, mode : DrawMode){
        if(mode == DrawMode.Preview && enabledSelectedOutline && selected){

        }
    }

}