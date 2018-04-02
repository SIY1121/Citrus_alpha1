package objects

import annotation.CObject
import annotation.CProperty
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO
import javafx.stage.FileChooser
import properties.FileProperty
import ui.GlCanvas
import java.io.File

@CObject("画像")
class Image : DrawableObject(), FileProperty.ChangeListener {


    override val id = "citrus/image"
    override val name = "画像"

    @CProperty("ファイル",0)
    val file = FileProperty(listOf(
            FileChooser.ExtensionFilter("PNG", "*.png"),
            FileChooser.ExtensionFilter("JPG", "*.jpg")
    ))

    var texture :Texture? = null

    init{
        file.listener = this
    }

    override fun onChanged(file: String) {
        GlCanvas.instance.invoke(false,{
            texture = TextureIO.newTexture(File(file),false)
            texture?.enable(it.gl)
            false
        })
    }

    override fun onDraw(gl: GL2, mode: DrawMode) {
        super.onDraw(gl, mode)

        val tex = texture ?: return

        tex.bind(gl)

        gl.glBegin(GL2.GL_QUADS)
        gl.glTexCoord2d(0.0,0.0)
        gl.glVertex3d(-tex.width/2.0,-tex.height/2.0,0.0)
        gl.glTexCoord2d(0.0,1.0)
        gl.glVertex3d(-tex.width/2.0,tex.height/2.0,0.0)
        gl.glTexCoord2d(1.0,1.0)
        gl.glVertex3d(tex.width/2.0,tex.height/2.0,0.0)
        gl.glTexCoord2d(1.0,0.0)
        gl.glVertex3d(tex.width/2.0,-tex.height/2.0,0.0)
        gl.glEnd()
        gl.glBindTexture(GL.GL_TEXTURE_2D,0)
    }
}