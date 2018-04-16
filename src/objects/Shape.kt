package objects

import annotation.CObject
import annotation.CProperty
import com.jogamp.opengl.GL2
import javafx.scene.paint.Color
import properties.ColorProperty
import properties.MutableProperty
import properties.SelectableProperty
import ui.GlCanvas
import util.Statics

@CObject("図形", "607D8BFF", "/assets/ic_shape.png")
class Shape : DrawableObject() {
    enum class Type {
        Ellipse, Rectangle, Triangle, Background
    }

    override val id = "citrus/shape"
    override val name = "図形"

    @CProperty("種類", 0)
    val selectableProperty = SelectableProperty(listOf("円", "四角形", "三角形", "背景"))

    @CProperty("色",1)
    val color = ColorProperty(Color.WHITE)

    @CProperty("サイズ", 2)
    val size = MutableProperty(min = 0.0, def = 100.0)


    init {
        displayName = "[図形]"
        selectableProperty.listener = object : SelectableProperty.ChangeListener {
            override fun onChanged(index: Int) {
                displayName = "[図形] ${selectableProperty.list[index]}"
            }
        }
    }

    override fun onDraw(gl: GL2, mode: DrawMode) {
        super.onDraw(gl, mode)
        gl.glColor4d(color.color.red,color.color.green,color.color.blue,color.color.opacity)
        when (selectableProperty.selectedIndex.toType()) {
            Type.Triangle -> {
                gl.glEnable(GL2.GL_POLYGON_SMOOTH)
                gl.glTranslated(-size.value(frame) / 2.0, -Math.pow(3.0, 0.5) * size.value(frame) / 2.0 / 3.0, 0.0)
                gl.glBegin(GL2.GL_TRIANGLES)
                gl.glVertex3d(0.0, 0.0, 0.0)
                gl.glVertex3d(size.value(frame), 0.0, 0.0)
                gl.glVertex3d(size.value(frame) / 2.0, Math.pow(3.0, 0.5) * size.value(frame) / 2.0, 0.0)
                gl.glEnd()
                gl.glDisable(GL2.GL_POLYGON_SMOOTH)
            }
            Type.Rectangle -> {
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex3d(-size.value(frame) / 2.0, -size.value(frame) / 2.0, 0.0)
                gl.glVertex3d(size.value(frame) / 2.0, -size.value(frame) / 2.0, 0.0)
                gl.glVertex3d(size.value(frame) / 2.0, size.value(frame) / 2.0, 0.0)
                gl.glVertex3d(-size.value(frame) / 2.0, size.value(frame) / 2.0, 0.0)
                gl.glEnd()
            }
            Type.Background -> {
                gl.glBegin(GL2.GL_QUADS)
                gl.glVertex3d(-Statics.project.width / 2.0, -Statics.project.height / 2.0, 0.0)
                gl.glVertex3d(-Statics.project.width / 2.0, Statics.project.height / 2.0, 0.0)
                gl.glVertex3d(Statics.project.width / 2.0, Statics.project.height / 2.0, 0.0)
                gl.glVertex3d(Statics.project.width / 2.0, -Statics.project.height / 2.0, 0.0)
                gl.glEnd()
            }
            Type.Ellipse -> {
                gl.glBegin(GL2.GL_POLYGON)
                for (i in 0 until 100) {
                    val rate = 2.0 * Math.PI * (i / 100.0)
                    gl.glVertex3d(Math.cos(rate) * size.value(frame), Math.sin(rate) * size.value(frame), 0.0)
                }
                gl.glEnd()
            }
        }
        gl.glColor4d(1.0,1.0,1.0,1.0)
    }

    fun Int.toType(): Type {
        return when (this) {
            0 -> Type.Ellipse
            1 -> Type.Rectangle
            2 -> Type.Triangle
            3 -> Type.Background
            else -> Type.Ellipse
        }
    }
}