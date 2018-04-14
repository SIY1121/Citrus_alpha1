package objects

import annotation.CObject
import annotation.CProperty
import com.jogamp.opengl.GL2
import properties.SelectableProperty
import ui.TimeLineObject

@CObject("図形")
class Shape : DrawableObject() {
    enum class Type {
        Triangle, Rectangle, Ellipse
    }

    override val id = "citrus/shape"
    override val name = "図形"

    @CProperty("種類",0)
    val selectableProperty = SelectableProperty(listOf("三角形", "四角形", "円"))


    init{
        displayName = "図形"
        selectableProperty.listener = object : SelectableProperty.ChangeListener{
            override fun onChanged(index: Int) {
                displayName = "図形 ${selectableProperty.list[index]}"
            }
        }
    }

    override fun onDraw(gl: GL2, mode: DrawMode) {
        super.onDraw(gl, mode)
        when (selectableProperty.selectedIndex.toType()) {
            Type.Triangle -> {
                gl.glBegin(GL2.GL_TRIANGLES)
                gl.glVertex3d(0.0,0.0,-1.0)
                gl.glVertex3d(100.0,0.0,-1.0)
                gl.glVertex3d(100.0,100.0,-1.0)
                gl.glEnd()

            }
            Type.Rectangle -> {
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex3d(-960.0,-540.0,0.0)
                gl.glVertex3d(-960.0,540.0,0.0)
                gl.glVertex3d(960.0,540.0 ,0.0)
                gl.glVertex3d(960.0,-540.0 ,0.0)
                gl.glEnd()
            }
            Type.Ellipse -> {

            }
        }

    }
    fun Int.toType():Type{
        return when(this){
            0->Type.Triangle
            1->Type.Rectangle
            2->Type.Ellipse
            else ->Type.Triangle
        }
    }

    override fun onLayoutUpdate(mode : TimeLineObject.EditMode) {
        super.onLayoutUpdate(mode)
    }
}