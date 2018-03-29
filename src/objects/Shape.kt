package objects

import annotation.CObject
import com.jogamp.opengl.GL
import annotation.CProperty

@CObject("図形")
class Shape : DrawableObject() {
    enum class Type {
        Triangle, Rectangle, Ellipse
    }

    override val id = "citrus/shape"
    override val name = "図形"

    @CProperty("種類",0)
    val selectableProperty = SelectableProperty(listOf("三角形","四角形","円"))


    init{
        displayName = "図形"
    }

    override fun onDraw(gl: GL, mode: DrawMode) {
        super.onDraw(gl, mode)
        when (selectableProperty.selectedIndex.toType()) {
            Type.Triangle -> {

            }
            Type.Rectangle -> {

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

    override fun onLayoutUpdate() {
        super.onLayoutUpdate()
        displayName = "${selectableProperty.selectedIndex.toType()}"
    }
}