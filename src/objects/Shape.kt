package objects

import com.jogamp.opengl.GL

class Shape : DrawableObject() {
    enum class Type {
        Triangle, Rectangle, Ellipse
    }

    override val id = "citrus/shape"
    override val name = "図形"

    var type: Type = Type.Triangle
        set(value) {
            field = value
            displayName = "図形 $type"
        }

    init{
        displayName = "図形"
    }

    override fun onDraw(gl: GL, mode: DrawMode) {
        super.onDraw(gl, mode)
        when (type) {
            Type.Triangle -> {

            }
            Type.Rectangle -> {

            }
            Type.Ellipse -> {

            }
        }
    }
}